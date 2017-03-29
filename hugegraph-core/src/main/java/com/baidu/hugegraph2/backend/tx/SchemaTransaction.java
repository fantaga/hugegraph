package com.baidu.hugegraph2.backend.tx;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.hugegraph2.HugeGraph;
import com.baidu.hugegraph2.backend.id.Id;
import com.baidu.hugegraph2.backend.query.HugeQuery;
import com.baidu.hugegraph2.backend.serializer.TextBackendEntry;
import com.baidu.hugegraph2.backend.store.BackendEntry;
import com.baidu.hugegraph2.backend.store.BackendStore;
import com.baidu.hugegraph2.schema.HugeEdgeLabel;
import com.baidu.hugegraph2.schema.HugePropertyKey;
import com.baidu.hugegraph2.schema.HugeVertexLabel;
import com.baidu.hugegraph2.type.define.Cardinality;
import com.baidu.hugegraph2.type.define.DataType;
import com.baidu.hugegraph2.type.schema.EdgeLabel;
import com.baidu.hugegraph2.type.schema.PropertyKey;
import com.baidu.hugegraph2.type.schema.VertexLabel;

public class SchemaTransaction extends AbstractTransaction {

    private static final Logger logger = LoggerFactory.getLogger(SchemaTransaction.class);

    // this could be an empty string, now setting a value just for test
    private static final String DEFAULT_COLUME = "default-colume";

    private static final String ID_COLUME = "_id";
    private static final String SCHEMATYPE_COLUME = "_schema";

    public SchemaTransaction(HugeGraph graph, BackendStore store) {
        super(graph, store);
        // TODO Auto-generated constructor stub
    }

    public List<HugePropertyKey> getPropertyKeys() {
        List<HugePropertyKey> propertyKeys = new ArrayList<HugePropertyKey>();

        HugeQuery query = new HugeQuery();
        query.has(SCHEMATYPE_COLUME,"PROPERTY");

        Iterable<BackendEntry> entries = query(query);
        entries.forEach(item -> {
            // TODO: use serializer instead
            TextBackendEntry entry = (TextBackendEntry) item;

            // TODO : util to covert
            String name = entry.column("name").toString();
            HugePropertyKey propertyKey = new HugePropertyKey(name, this);
            propertyKey.cardinality(Cardinality.valueOf(entry.column("cardinality").toString()));
            propertyKey.dataType(DataType.valueOf(entry.column("datatype").toString()));
            propertyKeys.add(propertyKey);
        });
        return propertyKeys;

    }

    public void addPropertyKey(HugePropertyKey propertyKey) {
        logger.debug("SchemaTransaction add property key, "
                + "name: " + propertyKey.name() + ", "
                + "dataType: " + propertyKey.dataType() + ", "
                + "cardinality: " + propertyKey.cardinality());

        Id id = this.idGenerator.generate(propertyKey);
        // TODO: use serializer instead
        TextBackendEntry entry = new TextBackendEntry(id);
        entry.column(ID_COLUME, id.asString());
        entry.column(SCHEMATYPE_COLUME, "PROPERTY");
        entry.column("name", propertyKey.name());
        entry.column("datatype", propertyKey.dataType().name());
        entry.column("cardinality", propertyKey.cardinality().toString());
        this.addEntry(entry);
    }

    public PropertyKey getPropertyKey(String name) {
        Id id = this.idGenerator.generate(new HugePropertyKey(name, null));
        BackendEntry entry = this.store.get(id);
        return this.serializer.readPropertyKey(entry);
    }

    public void removePropertyKey(String name) {
        logger.debug("SchemaTransaction remove property key " + name);

        Id id = this.idGenerator.generate(new HugePropertyKey(name, null));
        this.removeEntry(id);
    }

    public void addVertexLabel(HugeVertexLabel vertexLabel) {
        logger.debug("SchemaTransaction add vertex label, "
                + "name: " + vertexLabel.name());

        Id id = this.idGenerator.generate(vertexLabel);
        // TODO: use serializer instead
        this.addEntry(id, DEFAULT_COLUME, vertexLabel.toString());
    }

    public VertexLabel getVertexLabel(String name) {
        Id id = this.idGenerator.generate(new HugeVertexLabel(name, null));
        BackendEntry entry = this.store.get(id);
        return this.serializer.readVertexLabel(entry);
    }

    public void removeVertexLabel(String name) {
        logger.info("SchemaTransaction remove vertex label " + name);

        Id id = this.idGenerator.generate(new HugeVertexLabel(name, null));
        this.removeEntry(id);
    }

    public void addEdgeLabel(HugeEdgeLabel edgeLabel) {
        logger.debug("SchemaTransaction add edge label, "
                + "name: " + edgeLabel.name() + ", "
                + "multiplicity: " + edgeLabel.multiplicity() + ", "
                + "cardinality: " + edgeLabel.cardinality());

        Id id = this.idGenerator.generate(edgeLabel);
        // TODO: use serializer instead
        this.addEntry(id, DEFAULT_COLUME, edgeLabel.toString());
    }

    public EdgeLabel getEdgeLabel(String name) {
        Id id = this.idGenerator.generate(new HugeEdgeLabel(name, null));
        BackendEntry entry = this.store.get(id);
        return this.serializer.readEdgeLabel(entry);
    }

    public void removeEdgeLabel(String name) {
        logger.info("SchemaTransaction remove edge label " + name);

        Id id = this.idGenerator.generate(new HugeEdgeLabel(name, null));
        this.removeEntry(id);
    }

}