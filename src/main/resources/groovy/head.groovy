package test;

import org.linqs.psl.application.inference.LazyMPEInference;
import org.linqs.psl.application.learning.weight.maxlikelihood.LazyMaxLikelihoodMPE;
import org.linqs.psl.config.*
import org.linqs.psl.database.DataStore
import org.linqs.psl.database.Database;
import org.linqs.psl.database.Partition;
import org.linqs.psl.database.ReadOnlyDatabase;
import org.linqs.psl.database.rdbms.RDBMSDataStore
import org.linqs.psl.database.rdbms.driver.H2DatabaseDriver
import org.linqs.psl.database.rdbms.driver.H2DatabaseDriver.Type
import org.linqs.psl.groovy.PSLModel;
import org.linqs.psl.model.term.ConstantType;
import org.linqs.psl.model.atom.GroundAtom;
import org.linqs.psl.model.function.ExternalFunction;
import org.linqs.psl.ui.functions.textsimilarity.*
import org.linqs.psl.database.Queries;


ConfigManager cm = ConfigManager.getManager()
ConfigBundle config = cm.getBundle("basic-example")

def defaultPath = System.getProperty("java.io.tmpdir")
String dbpath = config.getString("dbpath", defaultPath + File.separator + "basic-example")
DataStore data = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, dbpath, true), config)

PSLModel m = new PSLModel(this, data)


