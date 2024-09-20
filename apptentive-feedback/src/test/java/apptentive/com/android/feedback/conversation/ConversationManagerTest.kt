package apptentive.com.android.feedback.conversation

import apptentive.com.android.TestCase
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.Logger
import apptentive.com.android.core.Provider
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.core.getTimeSeconds
import apptentive.com.android.encryption.Encryption
import apptentive.com.android.feedback.MockAndroidLoggerProvider
import apptentive.com.android.feedback.backend.ConversationFetchResponse
import apptentive.com.android.feedback.backend.ConversationService
import apptentive.com.android.feedback.backend.PayloadResponse
import apptentive.com.android.feedback.engagement.util.MockAndroidSharedPrefDataStore
import apptentive.com.android.feedback.engagement.util.MockFileSystem
import apptentive.com.android.feedback.mockAppRelease
import apptentive.com.android.feedback.mockDevice
import apptentive.com.android.feedback.mockPerson
import apptentive.com.android.feedback.mockSdk
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Configuration
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.model.MessageList
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.model.VersionHistory
import apptentive.com.android.feedback.payload.PayloadData
import apptentive.com.android.feedback.platform.DefaultStateMachine
import apptentive.com.android.feedback.platform.FileSystem
import apptentive.com.android.feedback.platform.SDKEvent
import apptentive.com.android.platform.AndroidSharedPrefDataStore
import apptentive.com.android.util.Result
import com.apptentive.android.sdk.conversation.ConversationData
import com.apptentive.android.sdk.conversation.LegacyConversationManager
import com.apptentive.android.sdk.conversation.LegacyConversationMetadata
import com.apptentive.android.sdk.conversation.LegacyConversationMetadataItem
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class ConversationManagerTest : TestCase() {

    @Before
    override fun setUp() {
        DependencyProvider.register<AndroidSharedPrefDataStore>(MockAndroidSharedPrefDataStore())
        DependencyProvider.register<ConversationRepository>(MockConversationRepository())
        DependencyProvider.register<Logger>(MockAndroidLoggerProvider())
    }

    @Test
    fun getActiveConversation() {
        DefaultStateMachine.reset()
        val fetchResponse = ConversationFetchResponse(
            id = "id",
            deviceId = "device_id",
            personId = "person_id",
            token = "token",
            encryptionKey = "encryption_key"
        )

        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)

        val conversationManager = createConversationManager(fetchResponse)

        var result: Result<Unit>? = null
        conversationManager.tryFetchConversationToken {
            result = it
        }

        assertThat(result).isEqualTo(Result.Success(Unit))

        val conversation: Conversation = conversationManager.activeConversation.value
        assertThat(conversation.conversationToken).isEqualTo(fetchResponse.token)
        assertThat(conversation.conversationId).isEqualTo(fetchResponse.id)
        assertThat(conversation.person.id).isEqualTo(fetchResponse.personId)
    }

    @Test
    @Ignore
    fun corruptedConversationData() {
    }

    @Test
    @Ignore
    fun conversationDataMigration() {
    }

    @Test
    fun testPersonUpdate() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        val conversationManager = createConversationManager()
        val customData = CustomData(content = mapOf("FirstKey" to "FirstValue", "SecondKey" to 2, "ThirdKey" to true))
        val newPerson = conversationManager.getConversation().person.copy(
            name = "name",
            email = "email",
            customData = customData
        )
        conversationManager.updatePerson(newPerson)
        assertEquals("name", getUpdatedPerson(conversationManager).name)
        assertEquals("email", getUpdatedPerson(conversationManager).email)
        assertEquals(customData, getUpdatedPerson(conversationManager).customData)

        // Remove a key from custom data
        val updatedCustomData = CustomData(customData.content.minus("FirstKey"))
        conversationManager.updatePerson(newPerson.copy(customData = updatedCustomData))
        assertEquals(updatedCustomData, getUpdatedPerson(conversationManager).customData)
    }

    private fun getUpdatedPerson(conversationManager: ConversationManager) =
        conversationManager.getConversation().person

    private fun getUpdatedDevice(conversationManager: ConversationManager) =
        conversationManager.getConversation().device

    @Test
    fun testCustomDataDeviceUpdate() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        val conversationManager = createConversationManager()
        val customData = CustomData(content = mapOf("FirstKey" to "FirstValue", "SecondKey" to 2, "ThirdKey" to false))
        val newDevice = conversationManager.getConversation().device.copy(
            customData = customData
        )
        conversationManager.updateDevice(newDevice)
        assertEquals(customData, getUpdatedDevice(conversationManager).customData)

        // Remove a key from custom data
        val updatedCustomData = CustomData(customData.content.minus("FirstKey"))
        conversationManager.updateDevice(newDevice.copy(customData = updatedCustomData))
        assertEquals(updatedCustomData.content.size, getUpdatedDevice(conversationManager).customData.content.size)
    }

    @Test
    fun testNonCustomDataDeviceUpdate() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        val conversationManager = createConversationManager()
        // The mockConversationRepository updates the mockDevice with osApiLevel = 31, osVersion = "12" hence there is a device udpate
        conversationManager.checkForDeviceUpdates(conversationManager.getConversation())
        assertTrue(conversationManager.isDeviceUpdateCheckDone)
    }

    @Test
    fun testAppReleaseSDKUpdate() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        val conversationManager = createConversationManager()
        conversationManager.onEncryptionSetupComplete()
        conversationManager.updateAppReleaseSDK(
            mockSdk,
            mockAppRelease.copy(
                versionName = "2.0.0"
            ),
            VersionHistory()
        )
        assertTrue(conversationManager.sdkAppReleaseUpdate.value)
    }

    @Test
    fun testCheckForSDKAppReleaseUpdates() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        val conversationManager = createConversationManager()
        conversationManager.checkForSDKAppReleaseUpdates(conversationManager.getConversation())
        /* mockAppRelease & mockSDK has updates which sets the appReleaseChanged & sdkChanged to true
        updateAppReleaseSDK() will be called and that sets sdkAppReleaseUpdate */
        assertTrue(conversationManager.sdkAppReleaseUpdate.value)
        assertEquals("Version name updated", conversationManager.getConversation().appRelease.versionName)
        assertEquals("Version updated", conversationManager.getConversation().sdk.version)
        assertEquals(true, conversationManager.isSDKAppReleaseCheckDone)
    }

    @Test
    fun testEngagementDataFetch() {
        DefaultStateMachine.reset()
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        val fetchResponse: ConversationFetchResponse =
            ConversationFetchResponse(
                id = "id",
                deviceId = "device_id",
                personId = "person_id",
                token = "token",
                encryptionKey = "encryption_key"
            )

        val conversationManager = createConversationManager(
            isDebuggable = true,
            mockConversationService = MockConversationService(
                fetchResponse,
                testTimeInterval = 222.22
            )
        )
        conversationManager.tryFetchConversationToken {}
        assertEquals(0.0, conversationManager.getConversation().engagementManifest.expiry, 0.0)
        conversationManager.tryFetchEngagementManifest()
        assertEquals(222.22, conversationManager.getConversation().engagementManifest.expiry, 0.0)
    }

    @Test
    fun testConversationReset() {
        DefaultStateMachine.reset()
        DependencyProvider.register<FileSystem>(MockFileSystem())
        // conversation serializer throws exception, checks should throttle, not throttled, anonymous conversation is created
        DefaultStateMachine.onEvent(SDKEvent.RegisterSDK)
        DefaultStateMachine.onEvent(SDKEvent.ClientStarted)
        val conversationManager = createConversationManager(shouldThrowException = true)
        assertEquals(conversationManager.getConversation().device, mockDevice)
        val expectedMessage = "Cannot load existing conversation, conversation reset throttled"
        // calling loadExistingConversation again throws serializer exception and gets throttled and throws Conversation serializer exception
        val exception = assertThrows(ConversationSerializationException::class.java) { conversationManager.loadExistingConversation() }

        assertTrue(exception.message == expectedMessage)
    }
}

internal fun createConversationManager(
    fetchResponse: ConversationFetchResponse =
        ConversationFetchResponse(
            id = "id",
            deviceId = "device_id",
            personId = "person_id",
            token = "token",
            encryptionKey = "encryption_key"
        ),
    isDebuggable: Boolean = false,
    shouldThrowException: Boolean = false,
    mockConversationService: MockConversationService = MockConversationService(fetchResponse)
): ConversationManager {
    return ConversationManager(
        conversationRepository = MockConversationRepository(shouldThrowException),
        conversationService = mockConversationService,
        legacyConversationManagerProvider = object : Provider<LegacyConversationManager> {
            override fun get() = MockLegacyConversationManager()
        },
        isDebuggable
    )
}

class MockConversationRepository(val throwException: Boolean = false) :
    ConversationRepository {
    private var conversation: Conversation? = null

    override fun createConversation(conversationId: String?, conversationToken: String?): Conversation {
        return Conversation(
            localIdentifier = "localIdentifier",
            conversationToken = null,
            conversationId = null,
            device = mockDevice,
            person = mockPerson,
            sdk = mockSdk,
            appRelease = mockAppRelease,
            engagementManifest = EngagementManifest(),
            engagementData = EngagementData()
        )
    }

    override fun saveConversation(conversation: Conversation) {
        this.conversation = conversation
    }

    override fun loadConversation(): Conversation? = if (throwException) throw ConversationSerializationException("", null)
    else conversation

    override fun getCurrentAppRelease(): AppRelease = mockAppRelease.copy(
        versionName = "Version name updated"
    )

    override fun getCurrentDevice(): Device = mockDevice.copy(osApiLevel = 31, osVersion = "12")

    override fun getCurrentSdk(): SDK = mockSdk.copy(
        version = "Version updated"
    )

    override fun updateEncryption(encryption: Encryption) {
    }

    override fun updateConversationRoster(conversationRoster: ConversationRoster) {
    }

    override fun saveRoster(conversationRoster: ConversationRoster) {
    }

    override fun initializeRepositoryWithRoster(): ConversationRoster {
        return ConversationRoster()
    }
}

internal class MockConversationService(
    private val response: ConversationFetchResponse,
    private val testTimeInterval: TimeInterval? = null
) :
    ConversationService {
    override fun fetchConversationToken(
        device: Device,
        sdk: SDK,
        appRelease: AppRelease,
        person: Person,
        callback: (Result<ConversationFetchResponse>) -> Unit
    ) {
        callback(Result.Success(response))
    }

    override fun fetchLoginConversation(
        device: Device,
        sdk: SDK,
        appRelease: AppRelease,
        person: Person,
        token: String,
        callback: (Result<ConversationFetchResponse>) -> Unit
    ) {
    }

    override fun fetchEngagementManifest(
        conversationToken: String,
        conversationId: String,
        callback: (Result<EngagementManifest>) -> Unit
    ) {
        callback(Result.Success(EngagementManifest(expiry = testTimeInterval ?: getTimeSeconds() + 1800)))
    }

    override fun fetchConfiguration(
        conversationToken: String,
        conversationId: String,
        callback: (Result<Configuration>) -> Unit
    ) {
        callback(Result.Success(Configuration(expiry = testTimeInterval ?: getTimeSeconds() + 1800)))
    }

    override fun loginSession(
        conversationId: String,
        jwtToken: String,
        callback: (Result<ConversationFetchResponse>) -> Unit
    ) {
        callback(Result.Success(ConversationFetchResponse(id = "", deviceId = "", personId = "", token = "", encryptionKey = "key")))
    }

    override fun getMessages(
        conversationToken: String,
        conversationId: String,
        lastMessageID: String,
        callback: (Result<MessageList>) -> Unit
    ) {
        callback(Result.Success(MessageList(messages = null, endsWith = "", hasMore = false)))
    }

    override fun getAttachment(remoteUrl: String, callback: (Result<ByteArray>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun sendPayloadRequest(
        payload: PayloadData,
        callback: (Result<PayloadResponse>) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}

private class MockLegacyConversationManager(val result: ConversationData? = null) : LegacyConversationManager {
    override fun loadLegacyConversationData(conversationMetadata: LegacyConversationMetadata?): ConversationData? {
        return result
    }

    override fun loadLegacyConversationMetadata(): LegacyConversationMetadata? {
        return null
    }

    override fun loadEncryptedLegacyConversationData(conversationMetadataItem: LegacyConversationMetadataItem?): ConversationData? {
        return null
    }
}
