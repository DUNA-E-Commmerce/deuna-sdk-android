package com.deuna.maven

import android.content.Context
import android.content.Intent
import com.deuna.maven.element.domain.*
import com.deuna.maven.shared.*
import com.deuna.maven.shared.domain.UserInfo
import com.deuna.maven.web_views.*
import com.deuna.maven.web_views.base.*
import java.lang.IllegalStateException

/**
 * Launch the Elements View
 *
 * @param userToken The user token
 * @param context The application or activity context
 * @param callbacks An instance of CheckoutCallbacks to receive checkout event notifications.
 * @param closeEvents (Optional) An array of CheckoutEvent values specifying when to close the elements activity automatically.
 * @param userInfo: (Optional) The basic user information. Pass this parameter if the userToken parameter is null.
 * @param cssFile (Optional) An UUID provided by DEUNA. This applies if you want to set up a custom CSS file.
 * @param types (Optional) A list of the widgets to be rendered.
 * Example:
 * ```
 * types = listOf(
 *    mapOf( "name" to "click_to_pay")
 * )
 * ```
 * @throws IllegalStateException if the passed userToken is not valid
 */
fun DeunaSDK.initElements(
    context: Context,
    callbacks: ElementsCallbacks,
    closeEvents: Set<ElementsEvent> = emptySet(),
    userToken: String? = null,
    userInfo: UserInfo? = null,
    cssFile: String? = null,
    types: List<Json> = emptyList(),
) {
    val baseUrl = this.environment.elementsBaseUrl

    ElementsActivity.setCallbacks(sdkInstanceId = sdkInstanceId, callbacks = callbacks)

    val queryParameters = mutableMapOf(
        QueryParameters.MODE.value to QueryParameters.WIDGET.value,
        QueryParameters.PUBLIC_API_KEY.value to publicApiKey
    )

    when {
        !userToken.isNullOrEmpty() -> queryParameters[QueryParameters.USER_TOKEN.value] = userToken
        userInfo != null && userInfo.isValid() -> {
            queryParameters.apply {
                put(QueryParameters.FIRST_NAME.value, userInfo.firstName)
                put(QueryParameters.LAST_NAME.value, userInfo.lastName)
                put(QueryParameters.EMAIL.value, userInfo.email)
            }
        }

        else -> {
            DeunaLogs.error(ElementsErrorMessages.MISSING_USER_TOKEN_OR_USER_INFO.message)
            callbacks.onError?.invoke(ElementsErrors.missingUserTokenOrUserInfo)
            return
        }
    }

    cssFile?.let {
        queryParameters[QueryParameters.CSS_FILE.value] = it
    }

    val path = types.firstOrNull()?.get(ElementsTypeKey.NAME.value)
        ?.takeIf { it is String && it.isNotEmpty() }
        ?.let { "/$it" } ?: ElementsTypeName.VAULT.value

    val elementUrl = Utils.buildUrl(baseUrl = "$baseUrl$path", queryParams = queryParameters)

    val intent = Intent(context, ElementsActivity::class.java).apply {
        putExtra(ElementsActivity.EXTRA_URL, elementUrl)
        putExtra(BaseWebViewActivity.EXTRA_SDK_INSTANCE_ID, sdkInstanceId)
        putStringArrayListExtra(
            BaseWebViewActivity.EXTRA_CLOSE_EVENTS,
            ArrayList(closeEvents.map { it.name })
        )
    }
    context.startActivity(intent)
}


/**
 * Closes the elements activity if it's currently running.
 */
@Deprecated(
    message = "This function will be removed in the future. Use close instead",
    replaceWith = ReplaceWith("close()")
)
fun DeunaSDK.closeElements() {
    close()
}