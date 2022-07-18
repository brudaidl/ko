package org.koitharu.kotatsu.favourites.ui.list

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.base.domain.ReversibleHandle
import org.koitharu.kotatsu.base.domain.reverseAsync
import org.koitharu.kotatsu.core.ui.titleRes
import org.koitharu.kotatsu.favourites.ui.categories.FavouriteCategoriesActivity
import org.koitharu.kotatsu.list.ui.MangaListFragment
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.utils.ext.withArgs

class FavouritesListFragment : MangaListFragment(), PopupMenu.OnMenuItemClickListener {

	override val viewModel by viewModel<FavouritesListViewModel> {
		parametersOf(categoryId)
	}

	private val categoryId: Long
		get() = arguments?.getLong(ARG_CATEGORY_ID) ?: NO_ID

	override val isSwipeRefreshEnabled = false

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewModel.sortOrder.observe(viewLifecycleOwner) { activity?.invalidateOptionsMenu() }
		viewModel.onItemsRemoved.observe(viewLifecycleOwner, ::onItemsRemoved)
	}

	override fun onScrolledToEnd() = Unit

	override fun onFilterClick(view: View?) {
		val menu = PopupMenu(view?.context ?: return, view)
		menu.setOnMenuItemClickListener(this)
		for ((i, item) in FavouriteCategoriesActivity.SORT_ORDERS.withIndex()) {
			menu.menu.add(Menu.NONE, Menu.NONE, i, item.titleRes)
		}
		menu.show()
	}

	override fun onMenuItemClick(item: MenuItem): Boolean {
		val order = FavouriteCategoriesActivity.SORT_ORDERS.getOrNull(item.order) ?: return false
		viewModel.setSortOrder(order)
		return true
	}

	override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
		mode.menuInflater.inflate(R.menu.mode_favourites, menu)
		return super.onCreateActionMode(mode, menu)
	}

	override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
		menu.findItem(R.id.action_save)?.isVisible = selectedItems.none {
			it.source == MangaSource.LOCAL
		}
		return super.onPrepareActionMode(mode, menu)
	}

	override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.action_remove -> {
				viewModel.removeFromFavourites(selectedItemsIds)
				mode.finish()
				true
			}
			else -> super.onActionItemClicked(mode, item)
		}
	}

	private fun onItemsRemoved(reversibleHandle: ReversibleHandle) {
		val message = viewModel.categoryName?.let {
			getString(R.string.removed_from_s, it)
		} ?: getString(R.string.removed_from_favourites)
		Snackbar.make(binding.recyclerView, message, Snackbar.LENGTH_LONG)
			.setAction(R.string.undo) { reversibleHandle.reverseAsync() }
			.show()
	}

	companion object {

		const val NO_ID = 0L
		private const val ARG_CATEGORY_ID = "category_id"

		fun newInstance(categoryId: Long) = FavouritesListFragment().withArgs(1) {
			putLong(ARG_CATEGORY_ID, categoryId)
		}
	}
}
