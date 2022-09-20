package frog.company.gamelifeapplication

import android.graphics.Color
import android.view.*
import androidx.recyclerview.widget.RecyclerView


class AdapterItem(
    private val dataList : ArrayList<Byte>) : RecyclerView.Adapter<AdapterItem.ViewHolder>(){

    private var clickListener: ClickListener? = null

    fun setOnItemClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onItemClick(position: Int, v: View?)
    }

    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view), View.OnClickListener{
        var view : View = view.findViewById(R.id.view)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            clickListener?.onItemClick(adapterPosition, v)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    val arrayColor = arrayListOf(Color.WHITE,Color.BLACK)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.setBackgroundColor(arrayColor[dataList[position].toInt()])
    }
}