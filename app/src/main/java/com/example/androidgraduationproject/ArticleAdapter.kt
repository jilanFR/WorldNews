package com.example.androidgraduationproject

import android.icu.number.NumberRangeFormatter.with
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidgraduationproject.model.Article
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_row.view.*

class ArticleAdapter (
    private var articleList: ArrayList<Article>
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    private val placeHolderImage = "https://pbs.twimg.com/profile_images/467502291415617536/SP8_ylk9.png"

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ArticleViewHolder {
        val itemView: View = LayoutInflater
            .from(viewGroup.context).inflate(R.layout.item_row, viewGroup, false)
        return ArticleViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return articleList.size
    }

    override fun onBindViewHolder(articleViewHolder: ArticleViewHolder, itemIndex: Int) {
        val article: Article = articleList.get(itemIndex)
        setPropertiesForArticleViewHolder(articleViewHolder, article)
        articleViewHolder.cardView.setOnClickListener {
            //do something
        }
    }

    private fun setPropertiesForArticleViewHolder(articleViewHolder: ArticleViewHolder, article: Article) {
        checkForUrlToImage(article, articleViewHolder)
        articleViewHolder.title.text = article.title
        articleViewHolder.description.text = article.description
    }

    private fun checkForUrlToImage(article: Article, articleViewHolder: ArticleViewHolder) {
        if (article.urlToImage == null || article.urlToImage.isEmpty()) {
            Picasso.get()
                .load(placeHolderImage)
                .centerCrop()
                .fit()
                .into(articleViewHolder.urlToImage)
        } else {
            Picasso.get()
                .load(article.urlToImage)
                .centerCrop()
                .fit()
                .into(articleViewHolder.urlToImage)
        }
    }

    fun setArticles(articles: ArrayList<Article>) {
        articleList = articles
        notifyDataSetChanged()
    }

    inner class ArticleViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        val cardView: CardView by lazy { view.card_view }
        val urlToImage: ImageView by lazy { view.article_urlToImage }
        val title: TextView by lazy { view.article_title }
        val description: TextView by lazy { view.article_description }
    }

}
