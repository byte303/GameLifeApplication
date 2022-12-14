package frog.company.gamelifeapplication

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import frog.company.gamelifeapplication.databinding.ActivityMainBinding
import frog.company.gamelifeapplication.helper.AppConst
import frog.company.gamelifeapplication.helper.EnumStatusGame
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private var gameStatus = EnumStatusGame.CREATE
    private var array : ArrayList<Byte> = ArrayList()
    private var temp : ArrayList<Byte> = ArrayList()

    private var timer: Timer? = null
    private var tTask: TimerTask? = null
    private var interval: Long = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        binding.imgStartPause.setOnClickListener {
            if(gameStatus == EnumStatusGame.GAME){
                gameStatus = EnumStatusGame.PAUSE
                binding.imgStartPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                timer?.cancel()
                tTask?.cancel()
            }else{
                gameStatus = EnumStatusGame.GAME
                binding.imgStartPause.setImageResource(R.drawable.ic_baseline_pause_24)
                timer = Timer()
                schedule()
            }
        }

        binding.imgRefresh.setOnClickListener {
            clear()
        }

        binding.list.layoutManager = GridLayoutManager(applicationContext, AppConst.WIDTH)
        binding.list.adapter = AdapterItem(array)

        (binding.list.adapter as AdapterItem).setOnItemClickListener(object :
            AdapterItem.ClickListener {
            override fun onItemClick(position: Int, v: View?) {
                if(gameStatus == EnumStatusGame.CREATE) {
                    array[position] = if (array[position] == 1.toByte())
                        0.toByte()
                    else
                        1.toByte()

                    binding.list.adapter?.notifyItemChanged(position)
                }
            }
        })
    }

    private fun schedule() {
        tTask?.cancel()
        if (interval > 0) {
            tTask = object : TimerTask() {
                override fun run() {
                    doAsync {
                        onStep()

                        binding.list.post {
                            binding.list.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
            timer?.schedule(tTask, 100, interval)
        }
    }

    private fun onStep() {
        val timeStart = System.currentTimeMillis()
        Log.i("MainActivity", timeStart.toString())
        for (i in 1 until AppConst.WIDTH - 1) { // ?????????????? ???????????? ?? ?????????????????? ??????????????
            for (j in 1 until AppConst.HEIGHT - 1) { // ?????????????? ???????????? ?? ?????????????????? ??????????????
                val index = j*AppConst.WIDTH+i // ???????????? ?????????????? ??????????????
                temp[index] = // ???????????? ?????????? ???????????????????? ?????? ??????????????????
                (
                    array[index-1] + // ???????????? ??????????
                    array[index+1] + // ???????????? ????????????
                    array[index-AppConst.WIDTH] + // ???????????? ??????
                    array[index-AppConst.WIDTH-1] + // ???????????? ?????? ??????????
                    array[index-AppConst.WIDTH+1] + // ???????????? ?????? ????????????
                    array[index+AppConst.WIDTH] + // ???????????? ??????
                    array[index+AppConst.WIDTH-1] + // ???????????? ?????? ??????????
                    array[index+AppConst.WIDTH+1] // ???????????? ?????? ????????????
                ).toByte()
            }
        }

        var count = 0 // ??????-???? ?????????? ????????????

        for (i in 1 until AppConst.WIDTH) {
            for (j in 1 until AppConst.HEIGHT) {
                val index = j * AppConst.WIDTH + i
                // ???????????????? ???? ????, ?????? ???? ?????? ?????? ?? ???????????? ???????? 2 ?????? 3 ?????????? ????????????
                val oldLife: Boolean = array[index] == 1.toByte() && (temp[index] == 2.toByte() || temp[index] == 3.toByte())
                // ???????????????? ???? ????, ?????? ???? ?????? ???????? ?? ???????????? ???????? 3 ?????????? ????????????
                val newLife: Boolean = array[index] == 0.toByte() && temp[index] == 3.toByte()

                // ???????? ???????? ???? ?????????????? ??????????, ???? ???????????? ????????
                if (oldLife || newLife) {
                    array[index] = 1.toByte()
                    count++ // ?????????????????????? ??????-???? ?????????? ????????????
                } else
                    array[index] = 0.toByte() // ?????????? ?????????????? ????????????
            }
        }
        // ????????????????, ?????? ???????? ??????????????????????
        if(count == 0){
            runOnUiThread{
                // ?????????????? ???????? ?? ?????????????? ??????????????????
                clear()
                Toast.makeText(applicationContext, "???????? ????????????????!", Toast.LENGTH_SHORT).show()
            }
        }
        val timeEnd = System.currentTimeMillis() - timeStart
        Log.i("MainActivity", timeEnd.toString())
    }

    private fun clear(){
        timer?.cancel()
        tTask?.cancel()
        gameStatus = EnumStatusGame.CREATE
        for(i in 0 until AppConst.WIDTH * AppConst.HEIGHT) {
            array[i] = 0.toByte()
            temp[i] = 0.toByte()
        }
        binding.list.adapter?.notifyDataSetChanged()
        binding.imgStartPause.setImageResource(R.drawable.ic_baseline_pause_24)
    }

    private fun init(){
        for(i in 0 until AppConst.WIDTH * AppConst.HEIGHT) {
            array.add(0.toByte())
            temp.add(0.toByte())
        }
    }

    class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        init {
            execute()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }
    }
}