package pl.ajtuss

import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

import static pl.ajtuss.GlueingCollec.glueing

@Unroll
class GlueingCollectorTest extends Specification {

    def "gluing items of #input with collecting items which is lower then #diff and result is #output"() {
        expect:
        def result = input.stream().collect(glueing({ l, n -> n - l < diff }))
        result == output

        where:
        input                     | diff | output
        []                        | 2    | []
        [1]                       | 2    | [[1]]
        [1, 2]                    | 1    | [[1], [2]]
        1..5                      | 1    | [[1], [2], [3], [4], [5]]
        [1, 2]                    | 2    | [[1, 2]]
        1..5                      | 2    | [[1, 2, 3, 4, 5]]
        [1, 2, 3, 5, 6, 8, 9, 10] | 2    | [[1, 2, 3], [5, 6], [8, 9, 10]]
    }

    def "gluing items of #input with collecting item which is lower then #diff and next sum is #output"() {
        expect:
        def result = input.stream().collect(glueing({ l, n -> n - l < diff }, Collectors.summingInt({ val -> val })))
        result == output

        where:
        input                     | diff | output
        []                        | 2    | []
        [1]                       | 2    | [1]
        [1, 2]                    | 1    | [1, 2]
        1..5                      | 2    | [15]
        [1, 2]                    | 2    | [3]
        [1, 2, 3, 5, 6, 8, 9, 10] | 2    | [6, 11, 27]
    }

}
