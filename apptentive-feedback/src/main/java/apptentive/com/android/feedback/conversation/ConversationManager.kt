package apptentive.com.android.feedback.conversation

import androidx.annotation.WorkerThread
import apptentive.com.android.core.BehaviorSubject
import apptentive.com.android.core.Observable
import apptentive.com.android.core.isInThePast
import apptentive.com.android.feedback.CONVERSATION
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.hasConversationToken
import apptentive.com.android.util.Log
import apptentive.com.android.util.Result

class ConversationManager(
    private val conversationRepository: ConversationRepository,
    private val conversationService: ConversationService
) {
    private val activeConversationSubject: BehaviorSubject<Conversation>
    val activeConversation: Observable<Conversation> get() = activeConversationSubject

    init {
        val conversation = loadActiveConversation()
        activeConversationSubject = BehaviorSubject(conversation)
        activeConversationSubject.observe(::saveConversation)
        activeConversationSubject.observe(::tryFetchEngagementManifest)
    }

    fun fetchConversationToken(callback: (result: Result<Unit>) -> Unit) {
        val conversation = activeConversation.value

        // if we have a conversation token - we're good
        if (conversation.hasConversationToken) {
            Log.v(CONVERSATION, "Conversation token already exists")
            callback(Result.Success(Unit))
            return
        }

        Log.v(CONVERSATION, "Fetching conversation token...")
        conversationService.fetchConversationToken(
            device = conversation.device,
            sdk = conversation.sdk,
            appRelease = conversation.appRelease,
            person = conversation.person
        ) {
            when (it) {
                is Result.Error -> {
                    Log.e(CONVERSATION, "Unable to fetch conversation token: ${it.error}")
                    callback(it)
                }
                is Result.Success -> {
                    Log.v(CONVERSATION, "Conversation token fetched successfully")
                    // update current conversation
                    val currentConversation = activeConversationSubject.value
                    activeConversationSubject.value = currentConversation.copy(
                        conversationToken = it.data.token,
                        conversationId = it.data.id,
                        person = currentConversation.person.copy(
                            id = it.data.personId
                        )
                    )

                    // let the caller know fetching was successful
                    callback(Result.Success(Unit))
                }
            }
        }
    }

    @Throws(ConversationSerializationException::class)
    @WorkerThread
    private fun loadActiveConversation(): Conversation {
        val existingConversation = conversationRepository.loadConversation()
        if (existingConversation != null) {
            Log.i(CONVERSATION, "Conversation already exists")
            return existingConversation
        }

        // no active conversations: create a new one
        Log.i(CONVERSATION, "Creating 'anonymous' conversation...")
        return conversationRepository.createConversation()
    }

    @WorkerThread
    private fun saveConversation(conversation: Conversation) {
        try {
            conversationRepository.saveConversation(conversation)
        } catch (exception: Exception) {
            Log.e(CONVERSATION, "Exception while saving conversation")
        }
    }

    @WorkerThread
    private fun tryFetchEngagementManifest(conversation: Conversation) {
        val manifest = conversation.engagementManifest
        if (!isInThePast(manifest.expiry)) {
            Log.d(CONVERSATION, "Engagement manifest up to date")
            return
        }

        val token = conversation.conversationToken
        val id = conversation.conversationId
        if (token != null && id != null) {
            conversationService.fetchEngagementManifest(
                conversationToken = token,
                conversationId = id
            ) {
                when (it) {
                    is Result.Success -> {
                        activeConversationSubject.value = activeConversationSubject.value.copy(
                            engagementManifest = it.data
                        )
                    }
                    is Result.Error -> {
                        Log.e(CONVERSATION, "Error while fetching engagement manifest", it.error)
                    }
                }
            }
        }
    }

    fun recordEvent(event: Event) {
        val conversation = activeConversationSubject.value
        activeConversationSubject.value = conversation.copy(
            engagementData = conversation.engagementData.addInvoke(
                event = event,
                versionName = conversation.appRelease.versionName,
                versionCode = conversation.appRelease.versionCode,
                lastInvoked = DateTime.now()
            )
        )
    }

    fun clear() {
        val conversation = activeConversationSubject.value
        activeConversationSubject.value = conversation.copy(
            engagementData = EngagementData()
        )
    }

    fun recordInteraction(interactionId: String) {
        val conversation = activeConversationSubject.value
        activeConversationSubject.value = conversation.copy(
            engagementData = conversation.engagementData.addInvoke(
                interactionId = interactionId,
                versionName = conversation.appRelease.versionName,
                versionCode = conversation.appRelease.versionCode,
                lastInvoked = DateTime.now()
            )
        )
    }
}