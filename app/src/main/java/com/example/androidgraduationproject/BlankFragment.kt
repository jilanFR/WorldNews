package com.example.androidgraduationproject

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidgraduationproject.databinding.FragmentBlankBinding
import com.example.androidgraduationproject.model.Article
import com.example.androidgraduationproject.model.TopHeadlines
import com.example.androidgraduationproject.repositry.TopHeadlineEndpoint
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Observable.fromIterable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_blank.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

class BlankFragment : Fragment() {

    private val ENDPOINT_URL by lazy { "https://newsapi.org/v2/" }
    private lateinit var topHeadlinesEndpoint: TopHeadlineEndpoint
    private lateinit var newsApiConfig: String
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var articleList: ArrayList<Article>
    private lateinit var userKeywordInput: String

    private lateinit var binding: FragmentBlankBinding

    // RxJava related fields
    private lateinit var topHeadlinesObservable: Observable<TopHeadlines>
    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentBlankBinding.inflate(layoutInflater)

        //Network request
        val retrofit: Retrofit = generateRetrofitBuilder()
        topHeadlinesEndpoint = retrofit.create(TopHeadlineEndpoint::class.java)

        newsApiConfig = resources.getString(R.string.api_key)

        articleList = ArrayList()
        articleAdapter = ArticleAdapter(articleList)

        //When the app is launched of course the user input is empty.
        userKeywordInput = ""
        //CompositeDisposable is needed to avoid memory leaks
        compositeDisposable = CompositeDisposable()

        binding.apply {
            binding.swipeRefresh.setOnRefreshListener(requireContext())
            swipeRefresh.setColorSchemeResources(R.color.purple_500)
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = articleAdapter
        }

        return binding.root

    }//END OF ON-CREATE-VIEW

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onRefresh() {
        checkUserKeywordInput()
    }

    private fun checkUserKeywordInput() {
        if (userKeywordInput.isEmpty()) {
            queryTopHeadlines()
        } else {
            getKeyWordQuery(userKeywordInput)
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        if (menu != null) {
//             val inflater: MenuInflater = menuInflater
//            // inflater.inflate(R.menu.main_menu, menu)
//
//            //Creates input field for the user search
//            setUpSearchMenuItem(menu)
//        }
//        return true
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        val inf = inflater
        if (menu != null) {
            inf.inflate(R.menu.main_menu, menu)
            setUpSearchMenuItem(menu)

        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setUpSearchMenuItem(menu: Menu) {
        val searchManager: SearchManager = (getSystemService(requireContext(),)) as SearchManager
        val searchView: SearchView = ((menu.findItem(R.id.action_search)?.actionView)) as SearchView
        val searchMenuItem: MenuItem = menu.findItem(R.id.action_search)

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = "Type any keyword to search..."
        searchView.setOnQueryTextListener(onQueryTextListenerCallback())
        searchMenuItem.icon.setVisible(false, false)
    }

    private fun onQueryTextListenerCallback(): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(userInput: String?): Boolean {
                return checkQueryText(userInput)
            }

            override fun onQueryTextChange(userInput: String?): Boolean {
                return checkQueryText(userInput)
            }
        }
    }

    private fun checkQueryText(queryText: String?): Boolean {
        if (queryText != null && queryText.length > 1) {
            userKeywordInput = queryText
            getKeyWordQuery(queryText)
        } else if (queryText != null || queryText == "") {
            userKeywordInput = ""
            queryTopHeadlines()
        }
        return false
    }
    private fun getKeyWordQuery(userKeywordInput: String?) {
        swipe_refresh.isRefreshing = true
        if (userKeywordInput != null && userKeywordInput.isNotEmpty()) {
            topHeadlinesObservable = topHeadlinesEndpoint.getUserSearchInput(newsApiConfig, userKeywordInput)
            subscribeObservableOfArticle()
        } else {
            queryTopHeadlines()
        }
    }
    private fun queryTopHeadlines() {
        swipe_refresh.isRefreshing = true
        topHeadlinesObservable = topHeadlinesEndpoint.getTopHeadlines("us", newsApiConfig)
        subscribeObservableOfArticle()
    }
    private fun subscribeObservableOfArticle() {
        articleList.clear()
        compositeDisposable.add(
            topHeadlinesObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap {
                    //class that emits a stream of data
                    Observable.fromIterable(it.articles)
                }
                .subscribeWith(createArticleObserver())
        )
    }
    private fun createArticleObserver(): DisposableObserver<Article> {
        return object : DisposableObserver<Article>() {
            override fun onNext(article: Article) {
                if (!articleList.contains(article)) {
                    articleList.add(article)
                }
                Log.d("createArticleObserver", "onNext: $article")
            }

            override fun onComplete() {
                showArticlesOnRecyclerView()
            }

            override fun onError(e: Throwable) {
                Log.e("createArticleObserver", "Article error: ${e.message}")
            }
        }
    }
    private fun showArticlesOnRecyclerView() {
        if (articleList.size > 0) {
            empty_text.visibility = View.GONE
            retry_fetch_button.visibility = View.GONE
            recycler_view.visibility = View.VISIBLE
            articleAdapter.setArticles(articleList)
        } else {
            recycler_view.visibility = View.GONE
            empty_text.visibility = View.VISIBLE
            retry_fetch_button.visibility = View.VISIBLE
            retry_fetch_button.setOnClickListener { checkUserKeywordInput() }
        }
        swipe_refresh.isRefreshing = false
    }


    private fun generateRetrofitBuilder(): Retrofit {

        return Retrofit.Builder()
            .baseUrl(ENDPOINT_URL)
            .addConverterFactory(GsonConverterFactory.create())
            //Add RxJava2CallAdapterFactory as a Call adapter when building your Retrofit instance
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }
}