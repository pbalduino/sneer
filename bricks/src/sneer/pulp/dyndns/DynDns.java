package sneer.pulp.dyndns;

import sneer.kernel.container.Brick;

public interface DynDns extends Brick {

	/**
	 * 
	 * @param hostname the full host name, for example, "test.dyndns.org"
	 * @param ip the new ip address
	 * @param user the user name
	 * @param password the user's password
	 * @return true if the record was updated or false if the record was already up to date
	 * @throws DynDnsException
	 */
	boolean update(String hostname, String ip, String user, String password) throws DynDnsException;

}
