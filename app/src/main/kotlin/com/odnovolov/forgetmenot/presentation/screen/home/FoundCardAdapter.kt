package com.odnovolov.forgetmenot.presentation.screen.home

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.common.getGradeColorRes
import com.odnovolov.forgetmenot.presentation.common.highlight
import kotlinx.android.synthetic.main.item_card_overview.view.*

class FoundCardAdapter(
    private val onCardClicked: (SearchCard) -> Unit
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    init {
        stateRestorationPolicy = PREVENT_WHEN_EMPTY
    }

    var items: List<SearchCard> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_overview, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val item: SearchCard = items[position]
        with(viewHolder.itemView) {
            questionTextView.text = item.card.question
                .highlight(item.questionMatchingRanges, context)
            questionTextView.isEnabled = !item.card.isLearned
            SpannableStringBuilder(item.card.answer)
            answerTextView.text = item.card.answer
                .highlight(item.answerMatchingRanges, context)
            answerTextView.isEnabled = !item.card.isLearned
            val gradeColorRes = getGradeColorRes(item.card.grade)
            gradeIcon.backgroundTintList = ContextCompat.getColorStateList(context, gradeColorRes)
            gradeIcon.text = item.card.grade.toString()
            cardView.setOnClickListener { onCardClicked(item) }
        }
    }

    override fun getItemCount(): Int = items.size
}