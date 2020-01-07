package com.odnovolov.forgetmenot.screen.home

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.QuizComposer
import com.odnovolov.forgetmenot.screen.home.HomeEvent.*
import com.odnovolov.forgetmenot.screen.home.HomeOrder.*

class HomeController : BaseController<HomeEvent, HomeOrder>() {
    private val queries: HomeControllerQueries = database.homeControllerQueries

    override fun handleEvent(event: HomeEvent) {
        when (event) {
            is SearchTextChanged -> {
                queries.setSearchText(event.searchText)
            }

            DisplayOnlyWithTasksCheckboxClicked -> {
                queries.toogleDisplayOnlyWithTasks()
            }

            is DeckButtonClicked -> {
                if (queries.hasAnySelectedDeckId().executeAsOne()) {
                    toggleDeckSelection(event.deckId)
                } else {
                    startExercise(listOf(event.deckId))
                }
            }

            is DeckButtonLongClicked -> {
                toggleDeckSelection(event.deckId)
            }

            is SetupDeckMenuItemClicked -> {
                queries.clearDeckSelection()

                queries.cleanDeckSettingsState()
                queries.initDeckSettingsState(event.deckId)
                issueOrder(NavigateToDeckSettings)
            }

            is RemoveDeckMenuItemClicked -> {
                removeDecks(listOf(event.deckId))
            }

            DecksRemovedSnackbarCancelActionClicked -> {
                queries.restoreDecks()
                queries.restoreCards()
            }

            StartExerciseMenuItemClicked -> {
                val deckIds = queries.getDeckSelection().executeAsList()
                startExercise(deckIds)
            }

            is SelectAllDecksMenuItemClicked -> {
                event.displayedCardIds.forEach(queries::addDeckToDeckSelection)
            }

            RemoveDecksMenuItemClicked -> {
                val deckIds = queries.getDeckSelection().executeAsList()
                removeDecks(deckIds)
            }

            ActionModeFinished -> {
                queries.clearDeckSelection()
            }
        }
    }

    private fun removeDecks(deckIds: List<Long>) {
        queries.dropTableCardBackup()
        queries.createTableCardBackup()
        queries.addCardsToBackup(deckIds)

        queries.dropTableDeckBackup()
        queries.createTableDeckBackup()
        queries.addDecksToBackup(deckIds)

        queries.deleteDecks(deckIds)
        issueOrder(ShowDeckRemovingMessage(deckIds.size))
    }

    private fun startExercise(deckIds: List<Long>) {
        queries.cleanExerciseCard()
        queries.initExerciseCard(deckIds)
        if (!queries.isThereAnyExerciseCard().executeAsOne()) {
            issueOrder(ShowNoCardsReadyForExercise)
            return
        }
        queries.cleanQuiz()
        QuizComposer.composeWhereItNeeds()
        queries.cleanAnswerInput()
        queries.initAnswerInput()
        queries.cleanExercise()
        queries.initExercise()
        queries.clearDeckSelection()
        queries.updateLastOpenedAt(deckIds)
        issueOrder(NavigateToExercise)
    }

    private fun toggleDeckSelection(deckId: Long) {
        if (queries.hasDeckInDeckSelection(deckId).executeAsOne()) {
            queries.deleteDeckFromDeckSelection(deckId)
        } else {
            queries.addDeckToDeckSelection(deckId)
        }
    }
}