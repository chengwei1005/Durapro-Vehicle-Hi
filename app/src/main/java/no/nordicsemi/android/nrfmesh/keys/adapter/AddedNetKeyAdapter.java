/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrfmesh.keys.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.NodeKey;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.CheckableRowItemBinding;
import no.nordicsemi.android.nrfmesh.utils.Utils;

public class AddedNetKeyAdapter extends RecyclerView.Adapter<AddedNetKeyAdapter.ViewHolder> {
    private final AsyncListDiffer<NetworkKey> differ = new AsyncListDiffer<>(this, new NetworkKeyDiffCallback());

    private final List<NetworkKey> addedNetKeys = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;
    private boolean enableSelection = true;

    public AddedNetKeyAdapter(@NonNull final LifecycleOwner owner,
                              @NonNull final List<NetworkKey> netKeys,
                              @NonNull final LiveData<ProvisionedMeshNode> meshNodeLiveData) {
        meshNodeLiveData.observe(owner, meshNode -> {
            final List<NetworkKey> networkKeys = new ArrayList<>(netKeys);
            Collections.sort(networkKeys, Utils.netKeyComparator);
            differ.submitList(networkKeys);

            addedNetKeys.clear();
            for (NodeKey nodeKey : meshNode.getAddedNetKeys()) {
                for (NetworkKey networkKey : networkKeys) {
                    if (nodeKey.getIndex() == networkKey.getKeyIndex()) {
                        addedNetKeys.add(networkKey);
                    }
                }
            }
            Collections.sort(addedNetKeys, Utils.netKeyComparator);
        });
    }

    public void setOnItemClickListener(final AddedNetKeyAdapter.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void enableDisableKeySelection(final boolean flag) {
        enableSelection = flag;
        notifyItemRangeChanged(0, differ.getCurrentList().size());
    }

    @NonNull
    @Override
    public AddedNetKeyAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new AddedNetKeyAdapter.ViewHolder(CheckableRowItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final AddedNetKeyAdapter.ViewHolder holder, final int position) {
        final NetworkKey key = differ.getCurrentList().get(position);
        holder.keyName.setText(key.getName());
        final String appKey = MeshParserUtils.bytesToHex(key.getKey(), false);
        holder.key.setText(appKey.toUpperCase());
        holder.check.setChecked(addedNetKeys.contains(key));
        holder.check.setEnabled(enableSelection);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(final NetworkKey appKey);
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        TextView keyName;
        TextView key;
        CheckBox check;

        private ViewHolder(@NonNull final CheckableRowItemBinding binding) {
            super(binding.getRoot());
            binding.icon.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_vpn_key_24dp));
            keyName = binding.title;
            key = binding.subtitle;
            check = binding.check;
            check.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    check.setChecked(!check.isChecked());
                    final NetworkKey key = differ.getCurrentList().get(getBindingAdapterPosition());
                    mOnItemClickListener.onItemClick(key);
                }
            });
        }
    }
}