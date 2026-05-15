package com.deuna.explore.integration

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import kotlin.math.abs
import com.deuna.explore.MainActivity
import com.deuna.explore.domain.ExplorePresentationMode
import com.deuna.explore.domain.ExploreWidget
import com.deuna.explore.presentation.ExploreTestTags

abstract class BaseExploreIntegrationTest {

    protected lateinit var device: UiDevice
    protected lateinit var webViewHelper: com.deuna.explore.integration.helpers.WebViewTestHelper

    protected lateinit var publicKey: String
    protected lateinit var privateKey: String
    protected lateinit var targetEnvironment: TestTargetEnvironment

    protected open fun merchantSetup(): TestMerchantSetup = TestMerchantSetup()

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
            // Swipe in middle zone to avoid triggering system gestures.
            device.swipe(w / 2, (h * 62) / 100, w / 2, (h * 36) / 100, 18)
            Thread.sleep(200)
        }
    }

    protected fun findObjectWithScroll(selector: BySelector, timeoutMs: Long): UiObject2? {
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

    protected fun configureDrawerAndApply(
        widget: ExploreWidget,
        presentationMode: ExplorePresentationMode,
    ) {
        openConfigurationDrawerOrFail()
        clickByResTagOrFail("explore.environment.${targetEnvironment.name.lowercase()}", fallbackText = targetEnvironment.drawerTitle)
        setDrawerKeysOrFail(publicKey = publicKey, privateKey = privateKey)

        selectWidgetInDrawer(widget)
        if (widget == ExploreWidget.VAULT_WIDGET) {
            device.findObject(By.res(ExploreTestTags.DEBUG_SET_TEST_EMAIL))?.click()
            Thread.sleep(150)
        }
        selectPresentationModeInDrawer(presentationMode)

        clickByResTagOrFail(ExploreTestTags.APPLY_CONFIGURATION_BUTTON, fallbackText = "Explorar")
    }

    protected fun fillIdentityDocumentOrFail(flowName: String) {
        val identityFilled = listOf(
            "Número de RFC",
            "RFC",
            "Número de documento",
            "Documento de identidad",
            "Identity document",
        ).any { webViewHelper.fillTextFieldByLabel("GODE561231GR8", it, timeout = 2000) }
        if (!identityFilled) throw AssertionError("Could not fill identity document field in $flowName flow")
    }

    protected fun waitForPaymentSuccess(maxTimeoutMs: Long = 60000): Boolean {
        val deadline = System.currentTimeMillis() + maxTimeoutMs
        while (System.currentTimeMillis() < deadline) {
            val successByTag = device.findObject(By.res(ExploreTestTags.PAYMENT_SUCCESS_TITLE))
            val successByText = device.findObject(By.textContains("Payment Successful"))
            if (successByTag != null || successByText != null) return true
            Thread.sleep(250)
        }
        return false
    }

    protected fun waitForVaultSuccess(maxTimeoutMs: Long = 60000): Boolean {
        val deadline = System.currentTimeMillis() + maxTimeoutMs
        while (System.currentTimeMillis() < deadline) {
            val successByTag = device.findObject(By.res(ExploreTestTags.CARD_SAVED_SUCCESS_TITLE))
            val successByText = device.findObject(By.textContains("Card saved successfully"))
            if (successByTag != null || successByText != null) return true
            Thread.sleep(250)
        }
        return false
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
        return TestMerchantKeysProvider.createKeys(setup = merchantSetup())
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

    private fun selectWidgetInDrawer(widget: ExploreWidget) {
        val debugSelector = when (widget) {
            ExploreWidget.PAYMENT_WIDGET -> Pair(ExploreTestTags.DEBUG_SELECT_WIDGET_PAYMENT, "T:Payment")
            ExploreWidget.VAULT_WIDGET -> Pair(ExploreTestTags.DEBUG_SELECT_WIDGET_VAULT, "T:Vault")
            ExploreWidget.VOUCHER_WIDGET -> Pair(ExploreTestTags.DEBUG_SELECT_WIDGET_VOUCHER, "T:Voucher")
            else -> null
        }
        if (debugSelector != null) {
            val byTag = device.wait(Until.findObject(By.res(debugSelector.first)), 4000)
            val node = byTag ?: device.wait(Until.findObject(By.textContains(debugSelector.second)), 3000)
                ?: throw AssertionError("Missing debug widget selector '${debugSelector.second}'")
            node.click()
            Thread.sleep(200)
            return
        }

        if (clickWidgetUsingUiScrollable(widget.title)) return

        // First, prefer visible text lookup with bounded swipes to avoid
        // exhausting the drawer and ending at the bottom.
        val byText = findObjectWithBoundedScroll(By.textContains(widget.title), maxSwipes = 10)
        if (byText != null) {
            runCatching { byText.click() }
                .onFailure {
                    val b = byText.visibleBounds
                    // Tap near the radio area on the left side of the row.
                    val radioX = (b.left + (b.width() * 0.12f)).toInt()
                    device.click(radioX, b.centerY())
                }
            Thread.sleep(250)
            return
        }

        val widgetTag = when (widget) {
            ExploreWidget.PAYMENT_WIDGET -> ExploreTestTags.WIDGET_PAYMENT_OPTION
            ExploreWidget.VAULT_WIDGET -> ExploreTestTags.WIDGET_VAULT_OPTION
            ExploreWidget.VOUCHER_WIDGET -> ExploreTestTags.WIDGET_VOUCHER_OPTION
            else -> "explore.widget.${widget.name.lowercase()}"
        }
        val node = findObjectWithScroll(By.res(widgetTag), timeoutMs = 20000)
        if (node != null) {
            runCatching { node.click() }
                .onFailure {
                    val b = node.visibleBounds
                    device.click(b.centerX(), b.centerY())
                }
            Thread.sleep(200)
            return
        }

        if (clickWidgetByRadioIndex(widget)) return
        throw AssertionError("Could not find widget option by text='${widget.title}' or tag=$widgetTag")
    }

    private fun selectPresentationModeInDrawer(mode: ExplorePresentationMode) {
        val modeTag = when (mode) {
            ExplorePresentationMode.MODAL -> ExploreTestTags.PRESENTATION_MODAL_OPTION
            ExplorePresentationMode.EMBEDDED -> ExploreTestTags.PRESENTATION_EMBEDDED_OPTION
        }
        val node = findObjectWithScroll(By.res(modeTag), timeoutMs = 20000)
            ?: throw AssertionError("Could not find presentation mode by tag=$modeTag")
        node.click()
    }

    private fun findObjectWithBoundedScroll(selector: BySelector, maxSwipes: Int): UiObject2? {
        repeat(maxSwipes + 1) { idx ->
            device.findObject(selector)?.let { return it }
            if (idx < maxSwipes) scrollDown(1)
        }
        return null
    }

    private fun openConfigurationDrawerOrFail() {
        runCatching { clickByResTagOrFail(ExploreTestTags.MENU_BUTTON, fallbackText = "Menu", timeoutMs = 4000) }
            .onSuccess { return }

        val byDesc = device.findObject(By.descContains("menu"))
            ?: device.findObject(By.descContains("Menu"))
            ?: device.findObject(By.descContains("Open"))
        if (byDesc != null) {
            byDesc.click()
            Thread.sleep(250)
            if (device.findObject(By.textContains("Configuration")) != null) return
        }

        val x = (device.displayWidth * 0.08).toInt()
        val y = (device.displayHeight * 0.07).toInt()
        device.click(x, y)
        Thread.sleep(300)
        if (device.findObject(By.textContains("Configuration")) != null) return

        throw AssertionError("Could not open configuration drawer")
    }

    private fun clickWidgetUsingUiScrollable(widgetTitle: String): Boolean {
        return runCatching {
            val scrollable = UiScrollable(UiSelector().scrollable(true))
            scrollable.setAsVerticalList()
            scrollable.scrollTextIntoView("Widget Type")
            scrollable.scrollTextIntoView(widgetTitle)

            val uiObj = device.findObject(UiSelector().textContains(widgetTitle))
            if (!uiObj.exists()) return false

            if (!uiObj.click()) {
                val b = uiObj.bounds
                val radioX = (b.left + (b.width() * 0.12f)).toInt()
                device.click(radioX, b.centerY())
            }
            Thread.sleep(250)
            true
        }.getOrDefault(false)
    }

    private fun clickWidgetByRadioIndex(widget: ExploreWidget): Boolean {
        val targetIndex = when (widget) {
            ExploreWidget.PAYMENT_WIDGET -> 0
            ExploreWidget.CHECKOUT_WIDGET -> 1
            ExploreWidget.VAULT_WIDGET -> 2
            ExploreWidget.NEXT_ACTION_WIDGET -> 3
            ExploreWidget.VOUCHER_WIDGET -> 4
            ExploreWidget.CLICK_TO_PAY_WIDGET -> 5
        }

        repeat(18) {
            val radios = device.findObjects(By.clazz("android.widget.RadioButton"))
                .sortedBy { it.visibleBounds.centerY() }
            if (radios.size > targetIndex) {
                runCatching { radios[targetIndex].click() }
                    .onFailure {
                        val b = radios[targetIndex].visibleBounds
                        device.click(b.centerX(), b.centerY())
                    }
                Thread.sleep(250)
                return true
            }
            scrollDown(1)
        }
        return false
    }
}
