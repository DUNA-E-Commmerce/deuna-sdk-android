package com.deuna.explore.integration

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import kotlin.math.abs
import com.deuna.explore.MainActivity

abstract class BaseExploreIntegrationTest {

    protected lateinit var device: UiDevice
    protected lateinit var webViewHelper: com.deuna.explore.integration.helpers.WebViewTestHelper

    protected lateinit var publicKey: String
    protected lateinit var privateKey: String
    protected lateinit var targetEnvironment: TestTargetEnvironment

    @org.junit.Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        webViewHelper = com.deuna.explore.integration.helpers.WebViewTestHelper(device)

        clearExploreSavedConfig()
        val keys = resolveOrCreateKeys()
        publicKey = keys.publicKey
        privateKey = keys.privateKey
        targetEnvironment = keys.targetEnvironment
    }

    protected fun launchActivity(): ActivityScenario<MainActivity> =
        ActivityScenario.launch(MainActivity::class.java)

    protected fun clickByResTagOrFail(tag: String, fallbackText: String? = null, timeoutMs: Long = 15000) {
        val node = device.wait(Until.findObject(By.res(tag)), timeoutMs)
            ?: fallbackText?.let { device.wait(Until.findObject(By.textContains(it)), 2000) }
            ?: throw AssertionError("Could not find UI node by tag=$tag")
        node.click()
    }

    protected fun clickByTextOrFail(text: String, timeoutMs: Long = 10000) {
        val node = device.wait(Until.findObject(By.text(text)), timeoutMs)
            ?: device.wait(Until.findObject(By.textContains(text)), 2000)
            ?: throw AssertionError("Could not find UI node by text=$text")
        node.click()
    }

    protected fun ensureSwitchOffByTag(tag: String, timeoutMs: Long = 10000) {
        val node = findObjectWithScroll(By.res(tag), timeoutMs)
            ?: throw AssertionError("Could not find switch by tag=$tag")
        if (node.isChecked) {
            node.click()
        }
    }

    protected fun ensureHidePayButtonOff(): Unit {
        dismissKeyboardIfVisible()
        val label = findLabelWithShortScroll("Hide Widget Pay Button")
            ?: throw AssertionError("Could not find 'Hide Widget Pay Button' row")
        val rowY = label.visibleBounds.centerY()
        val tapX = (device.displayWidth * 0.88).toInt()

        // First tap toggles current state; second verification uses status text if present.
        device.click(tapX, rowY)
        Thread.sleep(200)

        // If the row still exposes checked state nearby, toggle once more to force OFF.
        val switchByTag = device.findObject(By.res("explore.hidePayButtonSwitch"))
        if (switchByTag != null && switchByTag.isChecked) {
            switchByTag.click()
            Thread.sleep(150)
        }
    }

    protected fun scrollDown(times: Int = 1) {
        repeat(times) {
            val w = device.displayWidth
            val h = device.displayHeight
            // Keep swipe in upper/middle area to avoid hitting the on-screen keyboard.
            device.swipe(w / 2, (h * 60) / 100, w / 2, (h * 35) / 100, 20)
            Thread.sleep(200)
        }
    }

    private fun findObjectWithScroll(selector: BySelector, timeoutMs: Long): UiObject2? {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            device.findObject(selector)?.let { return it }
            scrollDown(1)
        }
        return null
    }

    private fun findLabelWithShortScroll(text: String): UiObject2? {
        repeat(10) {
            device.findObject(By.textContains(text))?.let { return it }
            scrollDown(1)
        }
        return null
    }

    protected fun setTextByResTagOrFail(tag: String, value: String, timeoutMs: Long = 15000) {
        val node = device.wait(Until.findObject(By.res(tag)), timeoutMs)
            ?: throw AssertionError("Could not find text field by tag=$tag")
        node.click()
        node.text = value
    }

    protected fun setDrawerKeysOrFail(publicKey: String, privateKey: String) {
        setTextFieldByLabelOrFail(label = "PUBLIC KEY", value = publicKey)
        setTextFieldByLabelOrFail(label = "PRIVATE KEY", value = privateKey)
        dismissKeyboardIfVisible()
    }

    private fun setTextFieldByLabelOrFail(label: String, value: String, timeoutMs: Long = 10_000) {
        val labelNode = device.wait(Until.findObject(By.textContains(label)), timeoutMs)
            ?: throw AssertionError("Label not found: $label")

        val labelBottom = labelNode.visibleBounds.bottom
        val labelCenterX = labelNode.visibleBounds.centerX()

        val field = device.findObjects(By.clazz("android.widget.EditText"))
            .filter { it.visibleBounds.top >= labelBottom - 40 }
            .minByOrNull { edit ->
                val vertical = edit.visibleBounds.top - labelBottom
                val horizontal = abs(edit.visibleBounds.centerX() - labelCenterX)
                vertical + horizontal / 2
            } ?: throw AssertionError("EditText not found for label: $label")

        field.click()
        field.clear()
        field.text = value
        Thread.sleep(200)
    }

    protected fun dismissKeyboardIfVisible() {
        device.findObject(By.textContains("Configuration"))?.click()
        Thread.sleep(150)
        if (isKeyboardVisible()) {
            device.pressBack()
            Thread.sleep(200)
        }
    }

    private fun isKeyboardVisible(): Boolean {
        return runCatching {
            val out = device.executeShellCommand("dumpsys input_method | grep mInputShown")
            out.contains("mInputShown=true")
        }.getOrDefault(false)
    }

    private fun clearExploreSavedConfig() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("explore_config", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    private fun resolveOrCreateKeys(): TestMerchantKeys {
        val byArgs = readOptionalKeysFromArgsOrEnv()
        if (byArgs != null) return byArgs
        return TestMerchantKeysProvider.createKeys()
    }

    private fun readOptionalKeysFromArgsOrEnv(): TestMerchantKeys? {
        val args = InstrumentationRegistry.getArguments()
        val publicByArg = args.getString(IntegrationTestConstants.PUBLIC_KEY_ARG)?.trim().orEmpty()
        val privateByArg = args.getString(IntegrationTestConstants.PRIVATE_KEY_ARG)?.trim().orEmpty()
        if (publicByArg.isNotEmpty() && privateByArg.isNotEmpty()) {
            return TestMerchantKeys(
                publicKey = publicByArg,
                privateKey = privateByArg,
                targetEnvironment = TestMerchantKeysProvider.targetEnvironmentFromBaseUrl(),
            )
        }

        val publicByEnv = System.getenv("DEUNA_PUBLIC_API_KEY")?.trim().orEmpty()
        val privateByEnv = System.getenv("DEUNA_PRIVATE_API_KEY")?.trim().orEmpty()
        if (publicByEnv.isNotEmpty() && privateByEnv.isNotEmpty()) {
            return TestMerchantKeys(
                publicKey = publicByEnv,
                privateKey = privateByEnv,
                targetEnvironment = TestMerchantKeysProvider.targetEnvironmentFromBaseUrl(),
            )
        }
        return null
    }
}
