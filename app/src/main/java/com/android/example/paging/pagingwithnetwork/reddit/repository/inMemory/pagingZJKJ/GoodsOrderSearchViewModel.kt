package com.android.example.paging.pagingwithnetwork.reddit.repository.inMemory.pagingZJKJ

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sgb.goods.entity.OrderListEntity
import com.sgb.goods.entity.SearchTagEntity
import com.sgb.goods.utils.base.viewmodel.BaseGoodsViewModel
import com.sgb.goods.view.activity.details.order.pagingsource.OrderSearchPagingSource
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.Flow

class GoodsOrderSearchViewModel(application: Application) : BaseGoodsViewModel(application) {
    var mSearchData: List<String> = arrayListOf()
    var mSearchTagEntities: MutableList<SearchTagEntity<*>> = arrayListOf()
    var orderType: Int = 0

    fun getPagingData(keyword: String): Flow<PagingData<OrderListEntity>> {
        return Pager(
                config = PagingConfig(10),
                pagingSourceFactory = { OrderSearchPagingSource(keyword, orderType.toString()) }
        )
                .flow
                .cachedIn(viewModelScope)
    }

    override fun accept(o: Disposable) {
    }
}