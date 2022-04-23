/*
 * This file is generated by jOOQ.
 */
package com.coffee4j.generated.tables;


import com.coffee4j.generated.CoffeeLog;
import com.coffee4j.generated.Keys;
import com.coffee4j.generated.tables.records.SchemasRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row4;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Schemas extends TableImpl<SchemasRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>coffee_log.schemas</code>
     */
    public static final Schemas SCHEMAS = new Schemas();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SchemasRecord> getRecordType() {
        return SchemasRecord.class;
    }

    /**
     * The column <code>coffee_log.schemas.id</code>.
     */
    public final TableField<SchemasRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>coffee_log.schemas.creator_id</code>.
     */
    public final TableField<SchemasRecord, Integer> CREATOR_ID = createField(DSL.name("creator_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>coffee_log.schemas.default</code>.
     */
    public final TableField<SchemasRecord, Byte> DEFAULT = createField(DSL.name("default"), SQLDataType.TINYINT.nullable(false), this, "");

    /**
     * The column <code>coffee_log.schemas.shared</code>.
     */
    public final TableField<SchemasRecord, Byte> SHARED = createField(DSL.name("shared"), SQLDataType.TINYINT.nullable(false), this, "");

    private Schemas(Name alias, Table<SchemasRecord> aliased) {
        this(alias, aliased, null);
    }

    private Schemas(Name alias, Table<SchemasRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>coffee_log.schemas</code> table reference
     */
    public Schemas(String alias) {
        this(DSL.name(alias), SCHEMAS);
    }

    /**
     * Create an aliased <code>coffee_log.schemas</code> table reference
     */
    public Schemas(Name alias) {
        this(alias, SCHEMAS);
    }

    /**
     * Create a <code>coffee_log.schemas</code> table reference
     */
    public Schemas() {
        this(DSL.name("schemas"), null);
    }

    public <O extends Record> Schemas(Table<O> child, ForeignKey<O, SchemasRecord> key) {
        super(child, key, SCHEMAS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : CoffeeLog.COFFEE_LOG;
    }

    @Override
    public Identity<SchemasRecord, Integer> getIdentity() {
        return (Identity<SchemasRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<SchemasRecord> getPrimaryKey() {
        return Keys.KEY_SCHEMAS_PRIMARY;
    }

    @Override
    public Schemas as(String alias) {
        return new Schemas(DSL.name(alias), this);
    }

    @Override
    public Schemas as(Name alias) {
        return new Schemas(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Schemas rename(String name) {
        return new Schemas(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Schemas rename(Name name) {
        return new Schemas(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, Integer, Byte, Byte> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
