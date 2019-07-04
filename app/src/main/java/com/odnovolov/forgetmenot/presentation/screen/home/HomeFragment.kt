package com.odnovolov.forgetmenot.presentation.screen.home

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import com.badoo.mvicore.android.AndroidTimeCapsule
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.home.di.HomeScreenComponent
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.ViewState
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.News
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.News.NavigateToExercise
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.UiEvent.*
import leakcanary.LeakSentry

class HomeFragment : BaseFragment<ViewState, UiEvent, News>() {

    @Inject lateinit var bindings: HomeFragmentBindings
    @Inject lateinit var adapter: DecksPreviewAdapter
    private lateinit var renameDialog: AlertDialog
    private lateinit var timeCapsule: AndroidTimeCapsule

    override fun onCreate(savedInstanceState: Bundle?) {
        timeCapsule = AndroidTimeCapsule(savedInstanceState)
        HomeScreenComponent.createWith(timeCapsule).inject(this)
        super.onCreate(savedInstanceState)
        bindings.setup(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        val toolbar: Toolbar = rootView.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.home_actions)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()
        initRenameDialog()
        initRecyclerAdapter()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupToolbar() {
        toolbar.setOnMenuItemClickListener { item: MenuItem? ->
            when (item?.itemId) {
                R.id.action_add -> {
                    showFileChooser()
                    true
                }
                else -> false
            }
        }
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("text/plain")
        startActivityForResult(intent, GET_CONTENT_REQUEST_CODE)
    }

    private fun initRenameDialog() {
        val onPositive = { dialogText: String -> emitEvent(RenameDialogPositiveButtonClicked(dialogText)) }
        val onNegative = { emitEvent(RenameDialogNegativeButtonClicked) }

        val contentView = activity!!.layoutInflater.inflate(R.layout.dialog_rename_deck, null)
        val renameDeckEditText: EditText = contentView.findViewById(R.id.renameDeckEditText)
        renameDialog = AlertDialog.Builder(context!!)
            .setTitle(R.string.enter_deck_name)
            .setView(contentView)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        renameDialog.setOnShowListener {
            renameDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener { onPositive.invoke(renameDeckEditText.text.toString()) }
            renameDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener { onNegative.invoke() }
        }
    }

    private fun initRecyclerAdapter() {
        decksPreviewRecycler.adapter = adapter
    }

    override fun accept(viewState: ViewState) {
        progressBar.visibility =
            if (viewState.isProcessing) {
                View.VISIBLE
            } else {
                View.GONE
            }
        if (viewState.isRenameDialogVisible) {
            renameDialog.show()
        } else {
            renameDialog.dismiss()
        }
    }

    override fun acceptNews(news: News) {
        when (news) {
            is NavigateToExercise -> findNavController().navigate(R.id.action_home_screen_to_exercise_screen)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == GET_CONTENT_REQUEST_CODE) {
            val uri = intent?.data ?: return
            val contentResolver: ContentResolver = context?.contentResolver ?: return
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val fileName = getFileName(contentResolver, uri)
            emitEvent(ContentReceived(inputStream, fileName))
        }
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor.use {
            if (cursor == null || !cursor.moveToFirst()) {
                return null
            }
            val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            return cursor.getString(nameIndex)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        timeCapsule.saveState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        LeakSentry.refWatcher.watch(this)
    }

    companion object {
        const val GET_CONTENT_REQUEST_CODE = 39
    }
}