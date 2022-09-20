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
    private var interval: Long = 1000

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
            timer?.schedule(tTask, 1000, interval)
        }
    }

    private fun onStep() {
        val timeStart = System.currentTimeMillis()
        Log.i("MainActivity", timeStart.toString())
        for (i in 1 until AppConst.WIDTH - 1) { // Убираем первую и последнюю строчку
            for (j in 1 until AppConst.HEIGHT - 1) { // Убираем первый и последний столбец
                val index = j*AppConst.WIDTH+i // Узнаём текущую позицию
                temp[index] = // Узнаём сумму окружающих нас элементов
                (
                    array[index-1] + // Узнаём слева
                    array[index+1] + // Узнаём справа
                    array[index-AppConst.WIDTH] + // Узнаём над
                    array[index-AppConst.WIDTH-1] + // Узнаём над слева
                    array[index-AppConst.WIDTH+1] + // Узнаём над справа
                    array[index+AppConst.WIDTH] + // Узнаём под
                    array[index+AppConst.WIDTH-1] + // узнаём под слева
                    array[index+AppConst.WIDTH+1] // Узнаём под справа
                ).toByte()
            }
        }

        var count = 0 // Кол-во живых клеток

        for (i in 1 until AppConst.WIDTH) {
            for (j in 1 until AppConst.HEIGHT) {
                val index = j * AppConst.WIDTH + i
                // Проверка на то, что он был жив и вокруг него 2 или 3 живых клетки
                val oldLife: Boolean = array[index] == 1.toByte() && (temp[index] == 2.toByte() || temp[index] == 3.toByte())
                // Проверка на то, что он был мёрт и вокруг него 3 живых клетки
                val newLife: Boolean = array[index] == 0.toByte() && temp[index] == 3.toByte()

                // если одно из условий верно, то клетка жива
                if (oldLife || newLife) {
                    array[index] = 1.toByte()
                    count++ // увеличиваем кол-во живых клеток
                } else
                    array[index] = 0.toByte() // иначе убиваем клетку
            }
        }
        // Проверка, что игра закончилась
        if(count == 0){
            runOnUiThread{
                // очищаем поле и выводим сообщение
                clear()
                Toast.makeText(applicationContext, "Игра окончена!", Toast.LENGTH_SHORT).show()
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