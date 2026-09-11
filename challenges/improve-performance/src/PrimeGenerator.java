import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrimeGenerator {

    public List<BigInteger> getPrimes(int size) {

    System.out.println("About to find " + size + " primes.");

    List<BigInteger> primes = IntStream.range(0, size)
        .parallel()
        .mapToObj(i ->
            new BigInteger(
                2000,
                16,
                ThreadLocalRandom.current())
        )
        .toList();

    System.out.println("Found all " + primes.size() + " primes.");

    return primes;
}
}