package cn.future.csoc.dataexchange;

import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * <p>created on 2015/7/2</p>
 *
 * @author Gonster
 */
public class HolaHamcrest {

    @Test
    public void helloHamcrest() {

        //is没有具体作用is(T value)等效于is(equalTo(T value))
        //is只是为了让assert的Matcher语句看起来更好理解，更像自然语言（其实就是更像英语_(:з」∠)_）
        assertThat(1, is(1));

        assertThat(1, anyOf(isA(int.class), notNullValue(), is(1)));
        assertThat(1, allOf(isA(int.class), notNullValue(), is(1)));
        assertThat(1, both(isA(int.class)).and(notNullValue()));
        assertThat(1, either(isA(int.class)).or(is(2)));

        //自定义Matcher
        BigDecimal bigDecimal = new BigDecimal("1");
        Matcher myBigDecimalMatcher = describedAs("a big decimal equal to %0",
                equalTo(bigDecimal), bigDecimal.toPlainString());
        assertThat(new BigDecimal("1"), myBigDecimalMatcher);

        assertThat(new ArrayList<>(Arrays.asList(new String[]{"a123", "a345"})),
                everyItem(startsWith("a")));

        //什么都能通过
        assertThat(1, anything(";) anything~"));

        //List和String
        assertThat(new ArrayList<>(Arrays.asList(new String[]{"a123","a345"})),
                hasItem(startsWith("a3")));
        assertThat(new ArrayList<>(Arrays.asList(new String[]{"a123","1","2","a345"})),
                hasItems("1", "2"));
        assertThat(new ArrayList<>(Arrays.asList(new String[]{"a123","1","2","a345"})),
                hasItems(endsWith("5"), containsString("5")));

        //调用对象的equals方法，1这里自动装箱了，调用Integer的equals方法
        assertThat(1, equalTo(1));

        assertThat(1, anyOf(any(int.class), any(Integer.class)));

        assertThat(new SocketTimeoutException(), instanceOf(IOException.class));

        assertThat(1, is(not(2)));

        assertThat(null, is(nullValue()));

        assertThat(1, notNullValue());

        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = sb;
        StringBuilder sb2 = new StringBuilder();
        assertThat(sb, sameInstance(sb1));
        assertThat(sb, not(sameInstance(sb2)));
        assertThat(sb, is(theInstance(sb1)));
        assertThat(sb, is(not(theInstance(sb2))));


    }
}
