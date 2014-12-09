package cm.aptoide.ptdev;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

import cm.aptoide.ptdev.adapters.V6.Displayable;
import cm.aptoide.ptdev.adapters.V6.Rows.StoreRow;
import cm.aptoide.ptdev.adapters.V6.V6StoresRecyclerAdapter;
import cm.aptoide.ptdev.webservices.Response;

/**
 * Created by asantos on 09-12-2014.
 */
public class MoreTopStoresActivity extends MoreBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.stores);
    }

    @Override
    protected MoreBaseFragment getFragment() {
        return new MoreTopStoresFragment();
    }

    public static class MoreTopStoresFragment extends MoreBaseFragment {
        private RecyclerView recyclerView;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_list_apps, container, false);
        }

        @Override
        public void onViewCreated(final View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            recyclerView = (RecyclerView) view.findViewById(R.id.list);

            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
            final List<Displayable> list = new ArrayList<>();

            recyclerView.setAdapter(new V6StoresRecyclerAdapter(view.getContext(),list));

            //TODO passa o request

            spiceManager.execute(null, "MoreTopStoresActivity", DurationInMillis.ONE_DAY, new RequestListener<Response.ListStores>() {
                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    view.findViewById(R.id.please_wait).setVisibility(View.GONE);
                    view.findViewById(R.id.error).setVisibility(View.VISIBLE);
                }

                @Override
                public void onRequestSuccess(Response.ListStores stores) {
                    ArrayList<Displayable> map = new ArrayList<>();

                    for(Response.ListStores.StoreGroup storeGroup : stores.datasets.getDataset().values()) {
                        for (Response.ListStores.Store store:storeGroup.data.list) {
                            StoreRow row = new StoreRow(getActivity());
                            row.header = store.name;
                            row.appscount = store.apps_count.intValue();
                            row.avatar = store.avatar;
                            row.name = store.name;
                            map.add(row);
                        }
                    }

                    list.clear();
                    list.addAll(map);

                    view.findViewById(R.id.please_wait).setVisibility(View.GONE);
                    view.findViewById(R.id.list).setVisibility(View.VISIBLE);

                    recyclerView.getAdapter().notifyDataSetChanged();

                }
            });
        }
    }
}