# UUID encoding of Swedish identity numbers.

An encoding of Swedish identity numbers for organisations, individuals,
etc. in a Version 1 date-time and MAC address UUID. The MAC address is fixed
and has the multicast bit set. In addition the least significant bit of `N`
must be set to 1.

The identity number is stored in the `time_low` and `time_mid` fields
encoded with 4 bits per digit to be readable as the original identity
in the canonical UUID hex representation.

A serial number is stored in `time_hi` to support multiple businesses registered
to the same individual natural person.

The type of identity is stored in the `t` field

```
                        ________________   _______________
                       /                \ /               \
                       00112233 4455 6677 8899 aabbccddeeff
                       -------- ---- ---- ---- ------------
                       xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx
                       iiiiiiii-iiii-1nnn-9xxt-d49a20d06c1a
                       \___________/ |\_/ |  | \__________/
                             |       | |  |  |       |
                         id number   | |  |  | fixed MAC
      stored as 1 digit per nybble   | |  |  |
                                     | |  |  |
version 1: date-time and MAC address / |  |  |
                                       |  |  |
                         serial number /  |  |
                                          |  |
   variant 1: 10x with x=0 -> 100 (hex 8) |  |
    lsb of N = 1, yielding N=1001 (hex 9) /  |
                                             |
           x=reserved (must be 0), t=id type /
```

## Examples

Enterprise identity number: `00556809-9963-1000-9000-d49a20d06c1a`

Social security number: `19410617-7753-1000-9001-d49a20d06c1a`



