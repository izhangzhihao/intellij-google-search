package com.github.izhangzhihao.google


import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.openapi.actionSystem.AnActionEvent

class GoogleSearchContributorFactory : SearchEverywhereContributorFactory<SearchResult> {
    override fun createContributor(initEvent: AnActionEvent): SearchEverywhereContributor<SearchResult> {
        return GoogleSearchContributor()
    }
}