package apptentive.com.android.feedback.conversation

import apptentive.com.android.encryption.Encryption
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.util.Factory
import apptentive.com.android.util.generateUUID

internal interface ConversationRepository {
    fun createConversation(conversationId: String? = null, conversationToken: String? = null): Conversation

    @Throws(ConversationSerializationException::class)
    fun saveConversation(conversation: Conversation)

    @Throws(ConversationSerializationException::class)
    fun loadConversation(): Conversation?

    @Throws(ConversationSerializationException::class)
    fun initializeRepositoryWithRoster(): ConversationRoster

    fun getCurrentAppRelease(): AppRelease

    fun getCurrentSdk(): SDK

    fun updateEncryption(encryption: Encryption)

    fun updateConversationRoster(conversationRoster: ConversationRoster)

    fun saveRoster(conversationRoster: ConversationRoster)
}

internal class DefaultConversationRepository(
    private val conversationSerializer: ConversationSerializer,
    private val appReleaseFactory: Factory<AppRelease>,
    private val personFactory: Factory<Person>,
    private val deviceFactory: Factory<Device>,
    private val sdkFactory: Factory<SDK>,
    private val manifestFactory: Factory<EngagementManifest>,
    private val engagementDataFactory: Factory<EngagementData>
) : ConversationRepository {
    override fun createConversation(conversationId: String?, conversationToken: String?): Conversation {
        return Conversation(
            localIdentifier = generateUUID(),
            conversationId = conversationId,
            conversationToken = conversationToken,
            person = personFactory.create(),
            device = deviceFactory.create(),
            appRelease = appReleaseFactory.create(),
            sdk = sdkFactory.create(),
            engagementManifest = manifestFactory.create(),
            engagementData = engagementDataFactory.create()
        )
    }

    @Throws(ConversationSerializationException::class)
    override fun saveConversation(conversation: Conversation) =
        conversationSerializer.saveConversation(conversation)

    @Throws(ConversationSerializationException::class)
    override fun loadConversation(): Conversation? = conversationSerializer.loadConversation()

    @Throws(ConversationSerializationException::class)
    override fun initializeRepositoryWithRoster(): ConversationRoster = conversationSerializer.initializeSerializer()

    override fun getCurrentAppRelease(): AppRelease = appReleaseFactory.create()

    override fun getCurrentSdk(): SDK = sdkFactory.create()

    override fun updateEncryption(encryption: Encryption) {
        conversationSerializer.setEncryption(encryption)
    }

    override fun updateConversationRoster(conversationRoster: ConversationRoster) {
        conversationSerializer.setRoster(conversationRoster)
    }

    override fun saveRoster(conversationRoster: ConversationRoster) {
        conversationSerializer.saveRoster(conversationRoster)
    }
}
