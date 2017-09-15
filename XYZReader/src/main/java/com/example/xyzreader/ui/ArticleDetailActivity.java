package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.util.Constants;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private Cursor mCursor;
    private int mPosId;
    private TextView mToolbarTitle;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;

    private FloatingActionButton mFabShare;

    private TextView mTxtTitle, mTxtAuthor, mTxtDate;
    private ImageView mImageViewPhoto;


    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article_detail);

        bindViews();

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().hasExtra(Constants.ARTICLE_POSITION)) {
                mPosId = getIntent().getIntExtra(Constants.ARTICLE_POSITION, 0);
            }
        }

        setSupportActionBar(mToolbar);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onDestroy() {
        mCursor.close();
        super.onDestroy();
    }

    private void bindViews() {
        mTxtTitle = (TextView) findViewById(R.id.article_title);
        mTxtAuthor = (TextView) findViewById(R.id.article_author);
        mTxtDate = (TextView) findViewById(R.id.article_date);
        mImageViewPhoto = (ImageView) findViewById(R.id.photo);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mPager = (ViewPager) findViewById(R.id.pager);
        mFabShare = (FloatingActionButton) findViewById(R.id.fab_share);
        mFabShare.setOnClickListener(this);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == 0) {
                    hideToolbarContents();
                } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
                    showToolbarContents();
                }
            }
        });

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageSelected(final int position) {
                setTitleData(position);
            }
        });
    }

    private void showToolbarContents() {
        mFabShare.hide();
        mToolbarTitle.animate()
                .alpha(1.0f)
                .setDuration(300)
                .start();
    }

    private void hideToolbarContents() {
        mFabShare.show();
        mToolbarTitle.animate()
                .alpha(0.0f)
                .setDuration(300)
                .start();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        setTitleData(mPosId);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));
        mPager.setCurrentItem(mPosId);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.fab_share:
                onShare();
                break;
        }
    }

    private void onShare() {
        mCursor.moveToPosition(mPager.getCurrentItem());
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText("Some sample text")
                .getIntent(), getString(R.string.action_share)));
    }

    private void setTitleData(int position) {

        mCursor.moveToPosition(position);

        mToolbarTitle.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        mCollapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));

        mTxtTitle.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        mTxtAuthor.setText(mCursor.getString(ArticleLoader.Query.AUTHOR));
        mTxtDate.setText(DateUtils.getRelativeTimeSpanString(
                mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL).toString());

        Glide.with(this)
                .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Palette palette = Palette.from(((BitmapDrawable) resource).getBitmap()).generate();
                        float[] hsv = new float[3];
                        int color = palette.getVibrantColor(palette.getMutedColor(palette.getDominantColor(getResources().getColor(R.color.dark_muted))));
                        Color.colorToHSV(color, hsv);
                        hsv[2] *= 0.8f;
                        int darkColor = Color.HSVToColor(hsv);
                        mCollapsingToolbarLayout.setStatusBarScrimColor(darkColor);
                        mCollapsingToolbarLayout.setContentScrimColor(color);
                        return false;
                    }
                }).into(mImageViewPhoto);
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance();
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }

    public String getArticleBody() {
        String body = "";
        if (mCursor != null) {
            body = mCursor.getString(ArticleLoader.Query.BODY);
        }
        return body;
    }
}
