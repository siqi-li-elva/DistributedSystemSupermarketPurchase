package dal;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DBCPDataSource {


  private static DataSource dataSources;

//  private static final String DB_HOSTNAME = System.getProperty("DB_HOSTNAME");
//  private static final String DB_HOSTNAME = "localhost";
//  private static final String PORT = System.getProperty("DB_PORT");
  private static final String PORT = "3306";
  private static final String DATABASE = "SuperMarketDatabase";
//  private static final String DB_USERNAME = System.getProperty("DB_USERNAME");
  private static final String DB_USERNAME = "siqi_root";
//  private static final String DB_PASSWORD = System.getProperty("DB_PASSWORD");
  private static final String DB_PASSWORD = "Aa135246789!";

  public static DataSource createPool(String db_hostname) {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", db_hostname,  PORT, DATABASE));
//    config.setJdbcUrl(String.format("jdbc:mysql:///%s", DB_NAME));
    config.setUsername(DB_USERNAME); // e.g. "root", "postgres"
    config.setPassword(DB_PASSWORD); // e.g. "my-password"
//    config.addDataSourceProperty("cloudSqlInstance", CLOUD_SQL_CONNECTION_NAME);
    config.addDataSourceProperty( "cachePrepStmts" , "true" );
    config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
    config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
    config.setMaximumPoolSize(15);
    // minimum number of idle connections Hikari maintains in the pool
    // additional connections will be setup to meet this minimum value, unless pool is full
//    config.setMinimumIdle(10);
    // max time to wait for a connection checkout, throw SQL Exception when exceeds the limit
//    config.setConnectionTimeout(60000); // 1 min
    // max time a connection can sit in the pool
    // connections sit idle for this max time are retried if minimumIdle is exceeded
//    config.setIdleTimeout(600000); // 10 min
    // max possible lifetime of a connection in the pool
    // connections that live longer than this max will be closed and reestablished between uses
    // this value be several minutes shorter than the database's timeout value to avoid unexpected terminations
    config.setMaxLifetime(1200000); // 20 min
    dataSources = new HikariDataSource(config);
    return dataSources;
  }
  static {

  }

  private DBCPDataSource() {}

  public static Connection getConnection() throws SQLException {
    return dataSources.getConnection();
  }
}
