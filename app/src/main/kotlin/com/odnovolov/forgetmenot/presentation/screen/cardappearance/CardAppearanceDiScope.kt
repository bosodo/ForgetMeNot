package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.example.CardAppearanceExampleViewModel

class CardAppearanceDiScope private constructor(
    val initialScreenState: CardAppearanceScreenState? = null
) {
    private val cardAppearance: CardAppearance = AppDiScope.get().cardAppearance

    private val screenState: CardAppearanceScreenState =
        initialScreenState ?: TODO()

    val controller = CardAppearanceController(
        cardAppearance,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = CardAppearanceViewModel(
        cardAppearance
    )

    val exampleViewModel = CardAppearanceExampleViewModel(
        cardAppearance,
        screenState
    )

    companion object : DiScopeManager<CardAppearanceDiScope>() {
        fun create(screenState: CardAppearanceScreenState) = CardAppearanceDiScope(screenState)

        override fun recreateDiScope() = CardAppearanceDiScope()

        override fun onCloseDiScope(diScope: CardAppearanceDiScope) {
            diScope.controller.dispose()
        }
    }
}