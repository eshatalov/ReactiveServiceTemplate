package com.github.template.config

import org.dbunit.dataset.datatype.AbstractDataType
import org.dbunit.dataset.datatype.DataTypeException
import org.dbunit.dataset.datatype.TypeCastException
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory
import org.postgresql.util.PGobject
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * Custom DataTypeFactory for PostgreSQL that properly handles JSONB columns.
 * Extends the default PostgreSQL factory to add JSONB support for Database Rider datasets.
 */
class JsonbDataTypeFactory : PostgresqlDataTypeFactory() {

    override fun createDataType(sqlType: Int, sqlTypeName: String?): org.dbunit.dataset.datatype.DataType {
        return try {
            // Handle JSONB type - use custom JsonbDataType
            if (sqlTypeName?.equals("jsonb", ignoreCase = true) == true) {
                JsonbDataType
            } else {
                super.createDataType(sqlType, sqlTypeName)
            }
        } catch (e: DataTypeException) {
            // Fallback for unknown types
            if (sqlTypeName?.equals("jsonb", ignoreCase = true) == true) {
                JsonbDataType
            } else {
                throw e
            }
        }
    }
}

/**
 * Custom DataType for JSONB columns.
 * Uses PGobject to properly set JSONB values in PostgreSQL.
 */
object JsonbDataType : AbstractDataType("JSONB", Types.OTHER, String::class.java, false) {

    override fun typeCast(value: Any?): Any? {
        if (value == null) {
            return null
        }
        return value.toString()
    }

    override fun compare(o1: Any?, o2: Any?): Int {
        return typeCast(o1)?.toString().orEmpty().compareTo(typeCast(o2)?.toString().orEmpty())
    }

    override fun getSqlValue(column: Int, resultSet: ResultSet): Any? {
        return resultSet.getString(column)
    }

    override fun setSqlValue(value: Any?, column: Int, statement: PreparedStatement) {
        try {
            if (value == null) {
                statement.setNull(column, Types.OTHER)
            } else {
                val pgObject = PGobject()
                pgObject.type = "jsonb"
                pgObject.value = value.toString()
                statement.setObject(column, pgObject)
            }
        } catch (e: Exception) {
            throw TypeCastException("Error casting value to JSONB: $value", e)
        }
    }
}
