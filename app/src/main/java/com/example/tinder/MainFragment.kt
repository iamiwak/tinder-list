package com.example.tinder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class MainFragment : Fragment() {
    private val linkOnImages = listOf(
        "https://sun9-69.userapi.com/impf/0_5Erg3mGMZCL51xA9uwmTPHTkdkXZGY2ed3ZA/Zo7rT1hSMmc.jpg?size=415x551&quality=96&sign=81acfc37c8e69cd026ed88ed99addd96&type=album"
//        "https://sun9-52.userapi.com/impf/P8Jwx7se-CHvmWHljgB3_X0BO0nuP9E2jnDIOg/4OeqhRdppKM.jpg?size=555x555&quality=96&sign=8530ff79dc68c4831133fa59fada4da0&type=album",
//        "https://sun9-46.userapi.com/impf/PZMm7maABo3XFauXM3HRqTCy_d6Z3s88FvmfNQ/YYvif7ZdKWU.jpg?size=398x802&quality=96&sign=3ba987c612a4d3cb7a4b41a28e364964&type=album",
//        "https://sun9-49.userapi.com/impf/u0h-vGJUVBbRGhJDs0_UlYvf9JUshJ2QhnBtWg/1mw_UdoKau8.jpg?size=810x1080&quality=96&sign=6f6dee8d411fe679de006b4ddda7eca8&type=album",
//        "https://sun9-west.userapi.com/sun9-14/s/v1/if1/FlEF_kXSbcuthMJKVUIvPl84haBS7Vfy04xpEEhQ6ghKqAjNFLYr1rcf8mSQPCePl9EugJl_.jpg?size=682x682&quality=96&type=album",
//        "https://sun9-west.userapi.com/sun9-13/s/v1/if1/pVeN-JdJO74a9IvmuEOMCLkf4QVVUIOPQsLw-nrL7tmANK49PiOpVp5t7JMcmq6Aoa3lHGEE.jpg?size=601x594&quality=96&type=album"
    )
    private lateinit var tinder: Tinder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.apply {
            tinder = findViewById(R.id.tinder)

            findViewById<Button>(R.id.refillButton).setOnClickListener {
                tinder.loadImage(linkOnImages)
            }
        }

        tinder.loadImage(linkOnImages)
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}