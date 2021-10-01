package com.example.covid_19;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import  androidx.core.util.Pair;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.covid_19.API.ApiClient;
import com.example.covid_19.API.ApiInterface;
import com.example.covid_19.model.Article;
import com.example.covid_19.model.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class home extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener  {
String API_KEY ="2701ed6502a14a41994d086bcf1fff8f";
       RecyclerView recyclerView ;
       RecyclerView.LayoutManager layoutManager;
        List<Article> articles = new ArrayList<>();
        Adapter adapter ;

        SwipeRefreshLayout swipeRefreshLayout;
        RelativeLayout errorlayout;
        ImageView errorimg;
        TextView errortitle , errormessage ;
        Button btnRetry;
        String TAG = home.class.getSimpleName();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
//        adapter.setOnItemClickListener(this);
        errorlayout =findViewById(R.id.errorlayout);
        errorimg =findViewById(R.id.errorimg);
        errortitle= findViewById(R.id.errortitle);
        errormessage =findViewById(R.id.errormessage);
        btnRetry =findViewById(R.id.retry);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
         recyclerView =findViewById(R.id.recyclerview);
         layoutManager = new LinearLayoutManager(home.this);
         recyclerView.setLayoutManager(layoutManager);
         recyclerView.setItemAnimator(new DefaultItemAnimator());
         recyclerView.setNestedScrollingEnabled(false);


        OnLoadingSwipeRefresh("");


    }

    public void Loadjeson( final String keyword){
        errorlayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String country = Utils.getCountry();
        String language =Utils.getLanguage();
        Call<News> call;
        if(keyword.length()>0){
            call=apiInterface.getNewsSearch(keyword,language ,"publishedAt",API_KEY);
        }else {
            call = apiInterface.getNews(country, API_KEY);
        }
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response != null){
                         if(!articles.isEmpty()){
                             articles.clear();
                         }
                         articles = response.body().getArticle();
                    adapter = new Adapter(articles , home.this );

                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                      initlistener();
                }else {
                    showErrorMessage(R.drawable.no_result , "no result" ,"Please Try Again ");
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                  swipeRefreshLayout.setRefreshing(false);
                showErrorMessage(R.drawable.no_result , "no result" ,"Please Try Again ");
            }
        });
    }
   private void initlistener(){
       adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
           @Override
           public void onItemClick(View view, int position) {
               ImageView imageView = view.findViewById(R.id.img);

               Intent intent = new Intent(home.this, NewsDetails.class);

               Article article = articles.get(position);
               intent.putExtra("url", article.getUrl());
               intent.putExtra("title", article.getTitle());
               intent.putExtra("img", article.getUrlToImage());
               intent.putExtra("date", article.getPublishedAt());
               intent.putExtra("source", article.getSource().getName());
               intent.putExtra("author", article.getAuthor());
             Pair<View, String> pair = Pair.create((View) imageView, ViewCompat.getTransitionName(imageView));
               ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(home.this, pair);


               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                   startActivity(intent, optionsCompat.toBundle());
               } else {
                   startActivity(intent);
               }
           }
           });



       }

       @Override
       public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main , menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =(SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchmenuitem =menu.findItem(R.id.action_search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search Latest News...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length()>2){
                    OnLoadingSwipeRefresh(query);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
               // OnLoadingSwipeRefresh(newText);
                return false;
            }
        });

        searchmenuitem.getIcon().setVisible(false ,false);

        return true;
    }
     public void onRefresh(){
         Loadjeson("");
     }

     public void  OnLoadingSwipeRefresh(String keyword){
         swipeRefreshLayout.post(new Runnable() {
             @Override
             public void run() {
                 Loadjeson(keyword);
             }
         });
     }

     private void showErrorMessage(int img , String title , String message ){
        if(errorlayout.getVisibility() == View.GONE){
            errorlayout.setVisibility(View.VISIBLE);
        }
        errorimg.setImageResource(img);
        errortitle.setText(title);
        errormessage.setText(message);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnLoadingSwipeRefresh("");
            }
        });
     }

}