package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingExistingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.deckexporter.DeckExporter
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.DeckContentController.Command
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.DeckContentController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.DeckContentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import java.io.IOException

class DeckContentController(
    private val deckExporter: DeckExporter,
    private val deckEditorScreenState: DeckEditorScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DeckContentEvent, Command>() {
    sealed class Command {
        class CreateFile(val fileName: String) : Command()
        object ShowDeckIsExportedMessage : Command()
        object ShowExportErrorMessage : Command()
    }

    override fun handle(event: DeckContentEvent) {
        when (event) {
            ExportButtonClicked -> {
                val fileName = deckEditorScreenState.deck.name
                sendCommand(CreateFile(fileName))
            }

            is OutputStreamOpened -> {
                val success: Boolean = deckExporter.export(
                    deck = deckEditorScreenState.deck,
                    outputStream = event.outputStream
                )
                sendCommand(if (success) ShowDeckIsExportedMessage else ShowExportErrorMessage)
            }

            SearchButtonClicked -> {
                navigator.navigateToSearchFromDeckSetup {
                    val cardsSearcher = CardsSearcher(deckEditorScreenState.deck)
                    SearchDiScope.create(cardsSearcher)
                }
            }

            is CardClicked -> {
                navigateToCardsEditor(event.cardId)
            }
        }
    }

    private fun navigateToCardsEditor(cardId: Long) {
        navigator.navigateToCardsEditorFromDeckEditor {
            val deck = deckEditorScreenState.deck
            val editableCards: List<EditableCard> =
                deck.cards.map { card -> EditableCard(card, deck) }
                    .plus(EditableCard(Card(generateId(), "", ""), deck))
            val position: Int = deck.cards.indexOfFirst { card -> card.id == cardId }
            val cardsEditorState = CardsEditor.State(editableCards, position)
            val cardsEditor = CardsEditorForEditingExistingDeck(deck, cardsEditorState)
            CardsEditorDiScope.create(cardsEditor)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}