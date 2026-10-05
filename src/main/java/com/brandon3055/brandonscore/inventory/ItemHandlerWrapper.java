package com.brandon3055.brandonscore.inventory;

import com.google.common.collect.MapMaker;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.HashMap;
import java.util.Map;

public class ItemHandlerWrapper extends SnapshotJournal<Map<Integer, ItemHandlerWrapper.Pending>> implements ResourceHandler<ItemResource> {
    private static final Map<IItemHandler, ItemHandlerWrapper> WRAPPERS = new MapMaker().weakKeys().weakValues().makeMap();

    private final IItemHandler handler;
    private Map<Integer, Pending> pending = new HashMap<>();

    private ItemHandlerWrapper(IItemHandler handler) {
        this.handler = handler;
    }

    public static ItemHandlerWrapper of(IItemHandler handler) {
        return WRAPPERS.computeIfAbsent(handler, ItemHandlerWrapper::new);
    }

    @Override
    public int size() {
        return handler.getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
        Pending p = pending.get(index);
        if (p != null && getAmountAsLong(index) == 0) {
            return ItemResource.EMPTY;
        }
        if (p != null && p.inserted > 0) {
            return p.resource;
        }
        return ItemResource.of(handler.getStackInSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        Pending p = pending.get(index);
        int count = handler.getStackInSlot(index).getCount();
        return p == null ? count : Math.max(0, count + p.inserted - p.extracted);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        int limit = handler.getSlotLimit(index);
        return resource.isEmpty() ? limit : Math.min(limit, resource.getMaxStackSize());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return handler.isItemValid(index, resource.toStack());
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        Pending p = pending.get(index);
        if (amount == 0 || (p != null && (p.extracted > 0 || !p.resource.equals(resource)))) {
            return 0;
        }
        int queued = p == null ? 0 : p.inserted;
        ItemStack remainder = handler.insertItem(index, resource.toStack(queued + amount), true);
        int inserted = Math.max(0, Math.min(amount, amount - remainder.getCount()));
        if (inserted > 0) {
            updateSnapshots(transaction);
            pending.put(index, new Pending(resource, queued + inserted, 0));
        }
        return inserted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        Pending p = pending.get(index);
        if (amount == 0 || (p != null && p.inserted > 0) || !resource.matches(handler.getStackInSlot(index))) {
            return 0;
        }
        int queued = p == null ? 0 : p.extracted;
        ItemStack simulated = handler.extractItem(index, queued + amount, true);
        int extracted = Math.max(0, Math.min(amount, simulated.getCount() - queued));
        if (extracted > 0) {
            updateSnapshots(transaction);
            pending.put(index, new Pending(resource, 0, queued + extracted));
        }
        return extracted;
    }

    @Override
    protected Map<Integer, Pending> createSnapshot() {
        return new HashMap<>(pending);
    }

    @Override
    protected void revertToSnapshot(Map<Integer, Pending> snapshot) {
        pending = snapshot;
    }

    @Override
    protected void onRootCommit(Map<Integer, Pending> originalState) {
        Map<Integer, Pending> ops = pending;
        pending = new HashMap<>();
        ops.forEach((slot, p) -> {
            if (p.inserted > 0) {
                handler.insertItem(slot, p.resource.toStack(p.inserted), false);
            } else if (p.extracted > 0) {
                handler.extractItem(slot, p.extracted, false);
            }
        });
    }

    protected record Pending(ItemResource resource, int inserted, int extracted) {}
}
