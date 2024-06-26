package com.example.happybirthday.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.happybirthday.R
import com.example.happybirthday.databinding.ItemFaqBinding
import com.example.happybirthday.model.Question

class QuestionAdapter(private val faqList: List<Question>) :
    RecyclerView.Adapter<QuestionAdapter.FaqViewHolder>() {

    class FaqViewHolder(val binding: ItemFaqBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFaqBinding.inflate(inflater, parent, false)
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faqItem = faqList[position]
        holder.binding.questionTextView.text = faqItem.question
        holder.binding.answerTextView.text = faqItem.answer
        holder.binding.questionTextView.setOnClickListener {
            if (holder.binding.answerTextView.visibility == View.GONE) {
                val animation = AnimationUtils.loadAnimation(holder.binding.root.context, R.anim.slide_up)
                holder.binding.answerTextView.startAnimation(animation)
                holder.binding.answerTextView.visibility = View.VISIBLE
            } else {
                val animation = AnimationUtils.loadAnimation(holder.binding.root.context, R.anim.slide_down)
                holder.binding.answerTextView.startAnimation(animation)
                holder.binding.answerTextView.visibility = View.GONE
            }
        }

    }

    override fun getItemCount(): Int {
        return faqList.size
    }
}