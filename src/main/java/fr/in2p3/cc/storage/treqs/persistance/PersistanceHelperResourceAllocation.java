package fr.in2p3.cc.storage.treqs.persistance;

import fr.in2p3.cc.storage.treqs.model.User;

public class PersistanceHelperResourceAllocation {

    private float allocation;
    private User user;

    public PersistanceHelperResourceAllocation(User user, float qtyAlloc) {
        this.user = user;
        this.allocation = qtyAlloc;
    }

    public float getAllocation() {
        return this.allocation;
    }

    public User getUser() {
        return this.user;
    }

}
