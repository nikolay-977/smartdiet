#!/bin/bash
set -e

echo "=== Initializing databases ==="

# Создаем базу данных для recommendation-service
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE nutrilogic_recommendations;
    GRANT ALL PRIVILEGES ON DATABASE nutrilogic_recommendations TO $POSTGRES_USER;
EOSQL

echo "Database nutrilogic_recommendations created successfully!"

# Исправляем права на схему public для nutrilogic_products
echo "=== Fixing schema permissions for nutrilogic_products ==="
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "nutrilogic_products" <<-EOSQL
    ALTER SCHEMA public OWNER TO $POSTGRES_USER;
    GRANT ALL ON SCHEMA public TO $POSTGRES_USER;
    GRANT CREATE ON SCHEMA public TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE nutrilogic_products TO $POSTGRES_USER;
EOSQL

# Исправляем права на схему public для nutrilogic_recommendations
echo "=== Fixing schema permissions for nutrilogic_recommendations ==="
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "nutrilogic_recommendations" <<-EOSQL
    ALTER SCHEMA public OWNER TO $POSTGRES_USER;
    GRANT ALL ON SCHEMA public TO $POSTGRES_USER;
    GRANT CREATE ON SCHEMA public TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE nutrilogic_recommendations TO $POSTGRES_USER;
EOSQL

echo "Permissions fixed!"

# Проверяем созданные базы
echo "=== Current databases ==="
psql -U "$POSTGRES_USER" -c "\l"