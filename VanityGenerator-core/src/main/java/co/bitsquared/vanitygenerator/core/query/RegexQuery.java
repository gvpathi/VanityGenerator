package co.bitsquared.vanitygenerator.core.query;

import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.network.Network;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * RegexQuery is the base Query type that is used to search for strings in ECKey addresses from regular expressions.
 * This class contains the base elements needed to search for addresses within a Search thread or PoolSearch thread.
 * This class is not capable of easily enforcing Prefix or Networks and should be used by those who know to use regular
 * expressions. To enforce Prefix / Network restrictions, you should use Query which is a more restricted Query type
 * that extends this class.
 * @see org.bitcoinj.core.ECKey
 * @see org.bitcoinj.core.Address
 * @see Query
 * @see NetworkQuery
 */
public class RegexQuery implements Comparable<RegexQuery> {

    protected Pattern pattern;
    protected boolean compressed;
    protected boolean findUnlimited;
    protected boolean searchForP2SH;
    protected GlobalNetParams netParams;

    protected RegexQuery(boolean compressed, boolean findUnlimited, boolean searchForP2SH) {
        this.compressed = compressed;
        this.findUnlimited = findUnlimited;
        this.searchForP2SH = searchForP2SH;
    }

    public RegexQuery(Pattern pattern, boolean compressed) {
        this(pattern, compressed, false, false);
    }

    public RegexQuery(Pattern pattern, boolean compressed, boolean findUnlimited, boolean searchForP2SH) {
        this.pattern = pattern;
        this.compressed = compressed;
        this.findUnlimited = findUnlimited;
        this.searchForP2SH = searchForP2SH;
    }

    public boolean matches(ECKey key, GlobalNetParams netParams) {
        if (!compressed) {
            key = key.decompress();
        }
        if (searchForP2SH) {
            return matches(Address.fromP2SHHash(netParams, key.getPubKeyHash()).toString());
        }
        if (this.netParams != null) {
            return matches(key.toAddress(this.netParams).toString());
        }
        return matches(key.toAddress(netParams).toString());
    }

    public boolean matches(String input) {
        return pattern.matcher(input).find();
    }

    public Pattern getPattern() {
        return pattern;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public boolean isFindUnlimited() {
        return findUnlimited;
    }

    public boolean isP2SH() {
        return searchForP2SH;
    }

    public void setCompression(boolean compression) {
        compressed = compression;
    }

    public void setFindUnlimited(boolean findUnlimited) {
        this.findUnlimited = findUnlimited;
    }

    public void updateNetwork(Network network) {
        updateNetParams(network.toGlobalNetParams());
    }

    public void updateNetParams(GlobalNetParams netParams) {
        this.netParams = netParams;
    }

    /**
     * Get the NetworkParameters associated with this RegexQuery. If there is no NP defined, the NP passed in will be
     * returned.
     * @param netParams - the NetworkParameters to use if this RegexQuery doesn't define one.
     * @return GlobalNetParams associated with this RegexQuery.
     */
    public GlobalNetParams getNetworkParameters(GlobalNetParams netParams) {
        return this.netParams == null ? netParams : this.netParams;
    }

    @Override
    public int hashCode() {
        int hash = 11;
        hash *= 23 + (findUnlimited ? 1 : 0);
        hash *= 23 + (compressed ? 1 : 0);
        hash *= 23 + (searchForP2SH ? 1 : 0);
        hash *= 23 + (pattern != null ? pattern.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RegexQuery && this.hashCode() == other.hashCode();
    }

    /**
     * Since it is hard to determine what actual query is held within a RegexQuery, we rely on two things:
     * Compressed -> Pattern
     * Compressed:
     *      Match: Return the patterns string comparison
     *      Don't Match: if this compression is true, return -1 since searching for a compressed addresses is faster. Otherwise return 1.
     */
    @Override
    public int compareTo(@Nonnull RegexQuery other) {
        if (hashCode() == other.hashCode()) return 0;
        if (compressed == other.compressed) {
            return pattern.toString().compareTo(other.toString());
        }
        return compressed ? -1 : 1;
    }

}
