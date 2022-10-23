package com.github.izhangzhihao.google

import com.intellij.ide.actions.SearchEverywherePsiRenderer
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.Processor
import com.intellij.util.ui.UIUtil
import java.awt.Color
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import javax.swing.JList
import javax.swing.ListCellRenderer


class GoogleSearchContributor : WeightedSearchEverywhereContributor<SearchResult> {

    override fun getSearchProviderId(): String = this.javaClass.packageName

    override fun getGroupName(): String = "Google"

    override fun getSortWeight(): Int = 1000000000

    override fun showInFindResults(): Boolean = true

    override fun getElementsRenderer(): ListCellRenderer<in SearchResult> {
        return object : SearchEverywherePsiRenderer(this) {
            override fun customizeNonPsiElementLeftRenderer(
                renderer: ColoredListCellRenderer<*>,
                list: JList<*>,
                value: Any,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ): Boolean {
                return try {
                    val fgColor: Color = list.foreground
                    val bgColor: Color = UIUtil.getListBackground()
                    val nameAttributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, fgColor)
                    val itemMatchers = getItemMatchers(list, value)
                    val apiNavigationItem: SearchResult = value as SearchResult
                    val name: String = apiNavigationItem.description
                    SpeedSearchUtil.appendColoredFragmentForMatcher(
                        name,
                        renderer,
                        nameAttributes,
                        itemMatchers.nameMatcher,
                        bgColor,
                        selected
                    )
                    true
                } catch (ex: Throwable) {
                    false
                }
            }
        }
    }

    override fun fetchWeightedElements(
        pattern: String,
        progressIndicator: ProgressIndicator,
        consumer: Processor<in FoundItemDescriptor<SearchResult>>
    ) {
        val baseUrl = "http://google.com/search?q="
        val encodedQuery: String = URLEncoder.encode(pattern.replace("\n", ""), "UTF-8")
        consumer.process(FoundItemDescriptor(SearchResult("Google", baseUrl + encodedQuery, "Search using Google with $encodedQuery"), 1000))
    }

    override fun getDataForItem(element: SearchResult, dataId: String): Any? = null

    override fun processSelectedItem(selected: SearchResult, modifiers: Int, searchText: String): Boolean {
        val desktop: Desktop? = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI.create(selected.url))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    override fun isShownInSeparateTab(): Boolean = true
}