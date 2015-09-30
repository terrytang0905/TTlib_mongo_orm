package com.newroad.mongodb.orm.transaction;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

/**
 * User: Denis Rystsov
 */
public class TransactionCRUD {

    public TransationLog create(DBCollection storage, DBObject value) {
    	TransationLog entity = new TransationLog();
        entity.version = 0;
        entity.value = value;
        entity.updated = null;
        entity.tx = null;

        DBObject record = new BasicDBObject();
        record.put("version", entity.version);
        record.put("value", entity.value);
        record.put("updated", entity.updated);
        record.put("tx", entity.tx);

        storage.insert(record).getLastError().throwOnError();

        entity.id = record.get("_id");

        return entity;
    }

    public TransationLog update(DBCollection storage, TransationLog entity) {
        int version = (Integer)entity.version;

        DBObject query = new BasicDBObject();
        query.put("_id", entity.id);
        query.put("version", version);

        DBObject record = new BasicDBObject();
        record.put("version", version+1);
        record.put("value", entity.value);
        record.put("updated", entity.updated);
        record.put("tx", entity.tx);

        WriteResult result = storage.update(query, record);

        result.getLastError().throwOnError();
        if (result.getN()==0) throw new RuntimeException();

        entity.version = version+1;
        return entity;
    }

    public TransationLog get(DBCollection storage, Object id) {
    	TransationLog entity = new TransationLog();
        entity.id = id;

        DBObject record = storage.findOne(new BasicDBObject("_id", id));
        if (record==null) return null;

        entity.version = record.get("version");
        entity.value = (DBObject)record.get("value");
        entity.updated = (DBObject)record.get("updated");
        entity.tx = record.get("tx");

        return entity;
    }

    public void delete(DBCollection storage, TransationLog entity) {
        DBObject query = new BasicDBObject();
        query.put("_id", entity.id);
        query.put("version", entity.version);

        WriteResult result = storage.remove(query);
        result.getLastError().throwOnError();
        if (result.getN()==0) throw new RuntimeException();
    }
}
