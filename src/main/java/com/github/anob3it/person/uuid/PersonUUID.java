package com.github.anob3it.person.uuid;

import java.io.Serializable;
import java.time.chrono.IsoChronology;
import java.util.UUID;

/**
 * UUID encoding of Swedish identity numbers.
 *
 * <p>Encodes Swedish identity numbers for organisations, individuals,
 * etc. in a Version 1 date-time and MAC address UUID. The MAC address is fixed
 * and has the multicast bit set. In addition the least significant bit of {@code N}
 * must be set to 1.</p>
 *
 * <p>The identity number is stored in the {@code time_low} and {@code time_mid} fields
 * encoded with 4 bits per digit to be readable as the original identity
 * in the canonical UUID hex representation.</p>
 *
 * <p>A serial number is stored in {@code time_hi} to support multiple businesses registered
 * to the same individual natural person.</p>
 *
 * <p>The type of identity is stored in the {@code t} field</p>
 *
 * <pre>
 *                         ________________   _______________
 *                        /                \ /               \
 *                        00112233 4455 6677 8899 aabbccddeeff
 *                        -------- ---- ---- ---- ------------
 *                        xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx
 *                        iiiiiiii-iiii-1nnn-9xxt-d59a20d06c1a
 *                        \___________/ |\_/ |  | \__________/
 *                              |       | |  |  |       |
 *                          id number   | |  |  | fixed MAC with
 *       stored as 1 digit per nybble   | |  |  | multicast bit set
 *                                      | |  |  |
 * version 1: date-time and MAC address / |  |  |
 *                                        |  |  |
 *                          serial number /  |  |
 *                                           |  |
 *    variant 1: 10x with x=0 -> 100 (hex 8) |  |
 *     lsb of N = 1, yielding N=1001 (hex 9) /  |
 *                                              |
 *            x=reserved (must be 0), t=id type /
 * </pre>
 */
public final class PersonUUID implements Serializable, Comparable<PersonUUID> {

    public static final int ID_ORGNR    = 0;
    public static final int ID_PERSNR   = 1;
    public static final int ID_SAMNR    = 2;
    public static final int ID_GDNR     = 3;

    private static final int ID_MAX     = 3;


    private static final long MSB_MASK      = 0x00000000_0000_F000;
    private static final long MSB_RESERVED  = 0x00000000_0000_1000L;

    private static final long NODE_ID       = 0x0_00_0_d59a20d06c1aL; // d4:9a:20:d0:6c:1a -- multicast d5:9a:20:d0:6c:1a
    private static final long LSB_MASK      = 0xf_ff_0_ffffffffffffL;
    private static final long LSB_RESERVED  = 0x9_00_0_000000000000L | NODE_ID;


    private final long id;
    private final int serial;
    private final int type;

    /**
     * Construct a person UUID from a UUID.
     * The UUID must be a valid person UUID.
     * @param uuid a person uuid
     * @throws IllegalArgumentException if {@code uuid} is not a valid person uuid
     */
    public PersonUUID(UUID uuid) {
        long lsb = uuid.getLeastSignificantBits();
        long msb = uuid.getMostSignificantBits();
        if (!isPersonUUID(msb, lsb)) {
            throw new IllegalArgumentException("Invalid person UUID: " + uuid);
        }

        this.id = decodeId(msb);
        this.serial = decodeSerial(msb);
        this.type = decodeType(lsb);
    }

    private static long decodeId(long msb) {
        long result = 0;
        long id = msb >>> 16;
        for (int i = 11; i >= 0; i--) {
            int digit = (int)((id >> (i << 2)) & 15);
            if (digit > 9) {
                throw new IllegalArgumentException(String.format("Invalid identity: %08x-%04x", id >>> 4, id & 0xffff));
            }
            result = result * 10 + digit;
        }

        return result;
    }

    private static int decodeSerial(long msb) {
        return (int)(msb & 0xfff);
    }

    private static int decodeType(long lsb) {
        int type = (int)((lsb >>> 48) & 15);
        if (type > ID_MAX) {
            throw new IllegalArgumentException("Invalid id type: " + type);
        }

        return type;
    }

    /**
     * Construct a person UUID from identity and type.
     * The serial number is set to zero (undefined).
     * @param id identity number
     * @param type type of identity number
     * @see #ID_ORGNR
     * @see #ID_PERSNR
     * @see #ID_SAMNR
     * @see #ID_GDNR
     */
    public PersonUUID(long id, int type) {
        this(id, 0, type);
    }

    /**
     * Construct a person UUID from identity and type.
     * @param id identity number
     * @param serial business serial number
     * @param type type of identity number
     * @see #ID_ORGNR
     * @see #ID_PERSNR
     * @see #ID_SAMNR
     * @see #ID_GDNR
     */
    public PersonUUID(long id, int serial, int type) {
        if ((id & 0xffff000000000000L) != 0) {
            throw new IllegalArgumentException("Invalid identity number: " + id);
        }

        if ((serial & ~0xfff) != 0) {
            throw new IllegalArgumentException("Invalid identity serial number: " + serial);
        }

        if (type < 0 || type > ID_MAX) {
            throw new IllegalArgumentException("Invalid identity type: " + type);
        }

        int checkDigit = (int)(id % 10);
        int computedCheckDigit = luhn((int)((id / 10) % 1_000_000_000));
        if (checkDigit != computedCheckDigit) {
            throw new IllegalArgumentException("Check digit mismatch in identity " + id + ", expected " + computedCheckDigit);
        }

        this.id = id;
        this.serial = serial;
        this.type = type;
    }

    private static int luhn(int nr) {
        int sum = 0;
        int shift = 1;
        while (nr != 0) {
            int n = (nr % 10) << shift;
            sum += n % 10 + n / 10;
            shift ^= 1;
            nr /= 10;
        }

        return (10 - (sum % 10)) % 10;
    }

    /**
     * Get identity number.
     * @return identity number.
     */
    public long getId() {
        return id;
    }

    /**
     * Get business serial number.
     * @return business serial number or zero.
     */
    public int getSerial() {
        return serial;
    }

    /**
     * Get identity number type
     * @return type code
     * @see #ID_ORGNR
     * @see #ID_PERSNR
     * @see #ID_SAMNR
     * @see #ID_GDNR
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the least significant 64 bits of this UUID's 128 bit value.
     * @return the least significant 64 bits of this UUID's 128 bit value.
     */
    public long getLeastSignificantBits() {
        return LSB_RESERVED | (((long)type) << 48);
    }

    /**
     * Returns the most significant 64 bits of this UUID's 128 bit value.
     * @return the most significant 64 bits of this UUID's 128 bit value.
     */
    public long getMostSignificantBits() {
        long msb = 0;
        long tmp = id;
        for (int i = 0; i < 12; i++, tmp /= 10) {
            msb |= (tmp % 10) << (i << 2);
        }

        msb = (msb << 16) | MSB_RESERVED | serial;
        return msb;
    }

    /**
     * Convert to UUID.
     * @return the person UUID in regular UUID representation.
     */
    public UUID toUUID() {
        return new UUID(getMostSignificantBits(), getLeastSignificantBits());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || o.getClass() != PersonUUID.class) {
            return false;
        }

        PersonUUID that = (PersonUUID) o;
        return id == that.id
                && serial == that.serial
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }

    @Override
    public String toString() {
        return toUUID().toString();
    }

    @Override
    public int compareTo(PersonUUID o) {
        long cmp = type - o.type;
        if (cmp == 0) {
            cmp = id - o.id;
            if (cmp == 0) {
                cmp = serial - o.serial;
            }
        }

        return cmp < 0 ? -1 : (cmp > 0 ? 1 : 0);
    }

    /**
     * Check if UUID is a person UUID.
     * @param uuid uuid to check
     * @return true if {@code uuid} is a person UUID
     */
    public static boolean isPersonUUID(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        return isPersonUUID(msb, lsb);
    }

    private static boolean isPersonUUID(long msb, long lsb) {
        return (msb & MSB_MASK) == MSB_RESERVED && (lsb & LSB_MASK) == LSB_RESERVED;
    }


    /**
     * Parse a person identity number.
     * @param str an identity number string or the string representation of a person UUID
     * @return PersonUUID
     * @throws IllegalArgumentException if {@code str} is not a valid identity number or person UUID.
     */
    public static PersonUUID parse(String str) {
        if (str.matches("\\d{10}|\\d{12}")) {
            return fromId(Long.parseLong(str));
        } else if (str.matches("\\d{6}-\\d{4}")) {
            return fromId(Long.parseLong(str.substring(0, 6)) * 1_0000 + Long.parseLong(str.substring(7)));
        } else if (str.matches("\\d{8}-\\d{4}")) {
            return fromId(Long.parseLong(str.substring(0, 8)) * 1_0000 + Long.parseLong(str.substring(9)));
        } else {
            return new PersonUUID(UUID.fromString(str));
        }
    }

    /**
     * Create a person UUID from an identity number.
     * The type of identity is inferred from the identity number.
     * @param id identity number
     * @return PersonUUID
     * @throws IllegalArgumentException if id is not a valid identity number
     */
    public static PersonUUID fromId(long id) {
        return fromId(id, 0);
    }

    /**
     * Create a person UUID from an identity number and serial number.
     * The type of identity is inferred from the identity number.
     * @param id identity number
     * @param serial serial number
     * @return PersonUUID
     * @throws IllegalArgumentException if id is not a valid identity number or serial number is invalid
     */
    public static PersonUUID fromId(long id, int serial) {
        if ((id / 1_000_0000L) == 302) {
            return new PersonUUID(id, serial, ID_GDNR);
        }

        long year = getYear(id);
        int month = getMonth(id);
        int day = getDay(id);
        long century = year / 100;
        if ((century == 0 || century == 16) && month >= 20) {
            return new PersonUUID(id, serial, ID_ORGNR);
        }

        if (century >= 18 && month >= 1 && month <= 12 && day >= 1 && day <= 31) {
            if (!isValidDate(year, month, day)) {
                throw new IllegalArgumentException("Invalid date in identity " + id + ": " + year + "-" + month + "-" + day);
            }
            return new PersonUUID(id, 0, ID_PERSNR);
        }

        if (century >= 19 && month >= 1 && month <= 12 && day >= 61 && day <= 91) {
            if (!isValidDate(year, month, day - 60)) {
                throw new IllegalArgumentException("Invalid date in identity " + id + ": " + year + "-" + month + "-" + (day - 60));
            }
            return new PersonUUID(id, serial, ID_SAMNR);
        }

        throw new IllegalArgumentException("Invalid identity number: " + id);
    }

    private static boolean isValidDate(long year, int month, int day) {
        if (day < 1 || month < 1 || month > 12) {
            return false;
        }

        int dim;
        if (month == 2) {
            dim = IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            dim = 30;
        } else {
            dim = 31;
        }

        return day <= dim;

    }

    private static int getDay(long nr) {
        return (int)((nr / 1_0000) % 100);
    }

    private static int getMonth(long nr) {
        return (int)((nr / 1_00_0000) % 100);
    }

    private static long getYear(long nr) {
        return nr / 1_00_00_0000;
    }

}
