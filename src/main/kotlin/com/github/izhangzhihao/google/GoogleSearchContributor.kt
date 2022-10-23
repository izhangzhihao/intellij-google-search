package com.github.izhangzhihao.google

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.intellij.ide.actions.SearchEverywherePsiRenderer
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.speedSearch.SpeedSearchUtil
import com.intellij.util.Processor
import com.intellij.util.ui.UIUtil
import org.apache.commons.io.IOUtils
import java.awt.Color
import java.awt.Desktop
import java.net.URI
import java.net.URL
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
        val baseUrl = "https://google.com/search?q="
        val encodedQuery: String = URLEncoder.encode(pattern.replace("\n", ""), "UTF-8")
        consumer.process(
            FoundItemDescriptor(
                SearchResult("Google", baseUrl + encodedQuery, "Google Search \"$pattern\""),
                1000
            )
        )
        consumer.process(
            FoundItemDescriptor(
                SearchResult(
                    "Google",
                    "$baseUrl$encodedQuery+site%3Astackoverflow.com",
                    "Search \"$pattern\" in Stack Overflow"
                ), 1000
            )
        )

        val url = URL("https://suggestqueries.google.com/complete/search?client=firefox&q=$encodedQuery")

        val con = url.openConnection()
        val `in` = con.getInputStream()
        val encoding: String = con.contentEncoding ?: "UTF-8"
        val body: String = IOUtils.toString(`in`, encoding)
        val res = Gson().fromJson(body, JsonArray::class.java)
        res[1].asJsonArray.forEach {
            val item = it.asString
            consumer.process(
                FoundItemDescriptor(
                    SearchResult(
                        "Google",
                        baseUrl + (URLEncoder.encode(item.replace("\n", ""), "UTF-8")),
                        "Google Search \"$item\""
                    ), 1000
                )
            )
        }
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