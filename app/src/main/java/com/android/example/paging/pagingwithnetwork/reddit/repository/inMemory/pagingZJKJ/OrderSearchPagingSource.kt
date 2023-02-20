package com.sgb.goods.view.activity.details.order.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.blankj.utilcode.util.ActivityUtils
import com.sgb.goods.api.GoodsNet
import com.sgb.goods.entity.OrderListEntity
import com.zjkj.lib.view.dialog.helper.DialogHelper
import retrofit2.awaitResponse

class OrderSearchPagingSource(private val keyword: String,
                              private val orderType: String) : PagingSource<Int, OrderListEntity>() {

    override suspend fun load(
            params: LoadParams<Int>
    ): LoadResult<Int, OrderListEntity> {
        return try {
            val page = params.key ?: 1 // set page 1 as default
            val pageSize = params.loadSize
            if (ActivityUtils.getTopActivity() != null && page == 1) {
                DialogHelper.showProgressDialog(ActivityUtils.getTopActivity(), null, "数据加载中...", false, false, null).setCanceledOnTouchOutside(false)
            }
            var repoResponse = GoodsNet.getInstance().goodsApi.getOrdersList("", page, 10, orderType, "", "", keyword)
                    .awaitResponse().body()
            DialogHelper.dismissProgressDialog()
            val prevKey = if (page > 1) page - 1 else null
            val nextKey = if (repoResponse?.data?.list?.isNotEmpty() == true) page + 1 else null

            if (null == prevKey && repoResponse?.data?.list?.isEmpty() == true && page == 1 && null == nextKey) {
                nextKey!!
            }

            LoadResult.Page(
                    data = repoResponse?.data?.list as List<OrderListEntity>,
                    prevKey = prevKey,
                    nextKey = nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, OrderListEntity>): Int? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
        //    just return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}