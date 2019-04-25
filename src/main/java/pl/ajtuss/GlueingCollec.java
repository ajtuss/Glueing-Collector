package pl.ajtuss;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class GlueingCollec {

  public static <T> Collector<T, ?, List<List<T>>> glueing(
      BiPredicate<? super T, ? super T> predicate) {
    return glueing(predicate, Collectors.toList());
  }

  public static <T, A, D> Collector<T, ?, List<D>> glueing(
      BiPredicate<? super T, ? super T> predicate,
      Collector<? super T, A, D> downstream) {
    return glueing(predicate, ArrayList::new, downstream);
  }

  public static <T, A, M extends List<D>, D> Collector<T, ?, List<D>> glueing(
      BiPredicate<? super T, ? super T> predicate,
      Supplier<M> accFactory,
      Collector<? super T, A, D> downstream) {
    return new GlueingCollector<>(predicate, accFactory, downstream);
  }


  private static class GlueingCollector<T, A, M extends List<D>, D> implements
      Collector<T, M, List<D>> {

    private Deque<T> buffer = new ArrayDeque<>();
    private final BiPredicate<? super T, ? super T> predicate;
    private Supplier<M> accFactory;
    private Collector<? super T, A, D> downstream;

    private GlueingCollector(
        BiPredicate<? super T, ? super T> predicate,
        Supplier<M> accFactory,
        Collector<? super T, A, D> downstream) {
      this.predicate = predicate;
      this.accFactory = accFactory;
      this.downstream = downstream;
    }

    @Override
    public Supplier<M> supplier() {
      return accFactory;
    }

    @Override
    public BiConsumer<M, T> accumulator() {
      return (acc, elem) -> {
        if (!buffer.isEmpty() && !predicate.test(buffer.peekLast(), elem)) {
          acc.add(collectBuffer());
          buffer.clear();
        }
        buffer.addLast(elem);
      };
    }

    @Override
    public BinaryOperator<M> combiner() {
      return (list1, list2) -> {
        list1.addAll(list2);
        return list1;
      };
    }

    @Override
    public Function<M, List<D>> finisher() {
      return acc -> {
        if (!buffer.isEmpty()) {
          acc.add(collectBuffer());
        }
        return acc;
      };
    }

    @Override
    public Set<Characteristics> characteristics() {
      return EnumSet.noneOf(Characteristics.class);
    }

    private D collectBuffer() {
      return buffer.stream().collect(downstream);
    }
  }
}

