/*
 * This file is generated by jOOQ.
 */
package com.coffee4j.generated.tables;


import com.coffee4j.generated.CoffeeLog;
import com.coffee4j.generated.Keys;
import com.coffee4j.generated.tables.records.BrewsRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row5;
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
public class Brews extends TableImpl<BrewsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>coffee_log.brews</code>
     */
    public static final Brews BREWS = new Brews();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BrewsRecord> getRecordType() {
        return BrewsRecord.class;
    }

    /**
     * The column <code>coffee_log.brews.id</code>.
     */
    public final TableField<BrewsRecord, String> ID = createField(DSL.name("id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>coffee_log.brews.user_id</code>.
     */
    public final TableField<BrewsRecord, String> USER_ID = createField(DSL.name("user_id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>coffee_log.brews.schema_id</code>.
     */
    public final TableField<BrewsRecord, String> SCHEMA_ID = createField(DSL.name("schema_id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>coffee_log.brews.field_id</code>.
     */
    public final TableField<BrewsRecord, String> FIELD_ID = createField(DSL.name("field_id"), SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>coffee_log.brews.field_value</code>.
     */
    public final TableField<BrewsRecord, String> FIELD_VALUE = createField(DSL.name("field_value"), SQLDataType.CLOB.nullable(false), this, "");

    private Brews(Name alias, Table<BrewsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Brews(Name alias, Table<BrewsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>coffee_log.brews</code> table reference
     */
    public Brews(String alias) {
        this(DSL.name(alias), BREWS);
    }

    /**
     * Create an aliased <code>coffee_log.brews</code> table reference
     */
    public Brews(Name alias) {
        this(alias, BREWS);
    }

    /**
     * Create a <code>coffee_log.brews</code> table reference
     */
    public Brews() {
        this(DSL.name("brews"), null);
    }

    public <O extends Record> Brews(Table<O> child, ForeignKey<O, BrewsRecord> key) {
        super(child, key, BREWS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : CoffeeLog.COFFEE_LOG;
    }

    @Override
    public UniqueKey<BrewsRecord> getPrimaryKey() {
        return Keys.KEY_BREWS_PRIMARY;
    }

    @Override
    public Brews as(String alias) {
        return new Brews(DSL.name(alias), this);
    }

    @Override
    public Brews as(Name alias) {
        return new Brews(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Brews rename(String name) {
        return new Brews(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Brews rename(Name name) {
        return new Brews(name, null);
    }

    // -------------------------------------------------------------------------
    // Row5 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row5<String, String, String, String, String> fieldsRow() {
        return (Row5) super.fieldsRow();
    }
}
