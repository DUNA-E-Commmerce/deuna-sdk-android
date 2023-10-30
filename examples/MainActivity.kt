
import com.deuna.maven.DeUnaSdk
import com.deuna.maven.domain.ElementType
import com.deuna.maven.domain.Environment
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myButton: Button = findViewById(R.id.myButton)
        val myTextView: TextView = findViewById(R.id.myTextView)

        myButton.setOnClickListener {
            // Llamar a sendOrder
            sendOrderToApi(myTextView)
        }
    }

    private fun sendOrderToApi(myTextView: TextView) {
        sendOrder(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    val responseBody = response.body() as? Map<*, *>
                    val orderMap = responseBody?.get("order") as? Map<*, *>
                    val orderId = orderMap?.get("order_id")?.toString()

                    if (orderId != null) {
                        DeUnaSdk.config(
                            apiKey = "2899059fa1ce2ab4324508c7fb7a4dcf1936cf8624ee00d533d50fc068bb4d984924997609ea662c6583a7bdb41207e3297969f610b28e03aa7acc018c01",
                            orderToken = orderId,
                            environment = Environment.DEVELOPMENT,
                            userToken = "",
                            elementType = ElementType.EXAMPLE
                        )

                        val rootView: View = findViewById(R.id.deuna_webview)

                        try {
                            val initPago = DeUnaSdk.initCheckout(rootView)

                            initPago.onSuccess = {
                                Log.d("Order Token", "Success")
                            }

                            initPago.onClose = {
                                Log.d("Order Token", "Cerrado")
                            }

                            initPago.onError = { order, error ->
                                if (error != null) {
                                    Log.d("Order Token", error)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("DeUnaSdkError", "Error al intentar abrir el WebView", e)
                        }

                    } else {
                        myTextView.text =
                            "¡Orden enviada con éxito, pero no se encontró el order_id!"
                    }
                } else {
                    myTextView.text = "Error en la respuesta: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                myTextView.text = "Error al enviar la orden: ${t.message}"
            }
        })
    }

}
