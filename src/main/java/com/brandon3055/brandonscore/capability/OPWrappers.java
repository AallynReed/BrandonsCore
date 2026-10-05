package com.brandon3055.brandonscore.capability;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Function;

/**
 * Created by brandon3055 on 16/9/19.
 */
public class OPWrappers {

    public static class FE implements IOPStorage {

        private final IEnergyStorage storage;

        public FE(IEnergyStorage storage) {
            this.storage = storage;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return storage.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return storage.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return storage.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return storage.getMaxEnergyStored();
        }

        @Override
        public long modifyEnergyStored(long amount) {
            amount = Math.min(Math.max(amount, Integer.MIN_VALUE), Integer.MAX_VALUE);
            if (amount > 0) {
                return receiveEnergy((int)amount, false);
            } else {
                return extractEnergy((int)-amount, false);
            }
        }

        @Override
        public boolean canExtract() {
            return storage.canExtract();
        }

        @Override
        public boolean canReceive() {
            return storage.canReceive();
        }
    }

    public static class EnergyHandlerWrapper extends SnapshotJournal<Long> implements EnergyHandler {

        private final IOPStorage storage;
        private long pending = 0;

        public EnergyHandlerWrapper(IOPStorage storage) {
            this.storage = storage;
        }

        @Override
        public long getAmountAsLong() {
            return Math.max(0, storage.getOPStored() + pending);
        }

        @Override
        public long getCapacityAsLong() {
            return storage.getMaxOPStored();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            if (amount == 0 || !storage.canReceive()) {
                return 0;
            }
            long queued = Math.max(0, pending);
            int inserted = (int) Math.max(0, Math.min(amount, storage.receiveOP(queued + amount, true) - queued));
            if (inserted > 0) {
                updateSnapshots(transaction);
                pending += inserted;
            }
            return inserted;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            if (amount == 0 || !storage.canExtract()) {
                return 0;
            }
            long queued = Math.max(0, -pending);
            int extracted = (int) Math.max(0, Math.min(amount, storage.extractOP(queued + amount, true) - queued));
            if (extracted > 0) {
                updateSnapshots(transaction);
                pending -= extracted;
            }
            return extracted;
        }

        @Override
        protected Long createSnapshot() {
            return pending;
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            pending = snapshot;
        }

        @Override
        protected void onRootCommit(Long originalState) {
            long amount = pending;
            pending = 0;
            if (amount > 0) {
                storage.receiveOP(amount, false);
            } else if (amount < 0) {
                storage.extractOP(-amount, false);
            }
        }
    }

    public static class ItemEnergyHandlerWrapper implements EnergyHandler {

        private final ItemAccess access;
        private final Function<ItemStack, IOPStorage> storageGetter;

        public ItemEnergyHandlerWrapper(ItemAccess access, Function<ItemStack, IOPStorage> storageGetter) {
            this.access = access;
            this.storageGetter = storageGetter;
        }

        @Override
        public long getAmountAsLong() {
            IOPStorage storage = storageGetter.apply(access.getResource().toStack());
            return storage == null ? 0 : storage.getOPStored();
        }

        @Override
        public long getCapacityAsLong() {
            IOPStorage storage = storageGetter.apply(access.getResource().toStack());
            return storage == null ? 0 : storage.getMaxOPStored();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            ItemStack stack = access.getResource().toStack();
            IOPStorage storage = storageGetter.apply(stack);
            if (amount == 0 || storage == null || !storage.canReceive()) {
                return 0;
            }
            int inserted = (int) storage.receiveOP(amount, false);
            if (inserted > 0 && access.exchange(ItemResource.of(stack), 1, transaction) == 1) {
                return inserted;
            }
            return 0;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            ItemStack stack = access.getResource().toStack();
            IOPStorage storage = storageGetter.apply(stack);
            if (amount == 0 || storage == null || !storage.canExtract()) {
                return 0;
            }
            int extracted = (int) storage.extractOP(amount, false);
            if (extracted > 0 && access.exchange(ItemResource.of(stack), 1, transaction) == 1) {
                return extracted;
            }
            return 0;
        }
    }
}
