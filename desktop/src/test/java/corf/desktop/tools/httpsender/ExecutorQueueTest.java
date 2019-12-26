package corf.desktop.tools.httpsender;

import org.apache.commons.collections4.IteratorUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import corf.base.common.KeyValue;
import corf.base.net.HttpClient;
import corf.base.text.CSV;
import corf.desktop.tools.common.Param;
import corf.desktop.tools.common.Param.Type;
import corf.desktop.tools.httpsender.Template.Batch;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static corf.base.net.HttpConstants.Method.POST;

public class ExecutorQueueTest {

    static final String DATA = """
                               10,11,12
                               20,21,22
                               30,31,32
                               """;

    @Test
    public void testHttpHeadersPlaceholdersReplaced() {
        var template = Template.create("test", "http://127.0.0.1", POST);
        template.setParams(Set.of(
                new TestParam("param1", Type.CONSTANT, null, "value1")
        ));
        var headers = Map.of(
                "header1", "%(param1)",
                "header2", "%(_csv1)",
                "header3", "%(_index0)",
                "header4", "%(unknown)"
        );

        var queue = new ExecutorQueue(template, CSV.from(DATA), headers);
        List<HttpClient.Request> result = IteratorUtils.toList(queue);

        assertThat(result).hasSize(3);
        assertThat(queue.size()).isEqualTo(3);
        assertThat(result.get(0).headers()).isEqualTo(
                Map.of("header1", "value1", "header2", "11", "header3", "0", "header4", "%(unknown)")
        );
        assertThat(result.get(1).headers()).isEqualTo(
                Map.of("header1", "value1", "header2", "21", "header3", "1", "header4", "%(unknown)")
        );
        assertThat(result.get(2).headers()).isEqualTo(
                Map.of("header1", "value1", "header2", "31", "header3", "2", "header4", "%(unknown)")
        );
    }

    @Test
    public void testUriPlaceholdersReplaced() {
        var template = Template.create("test", "http://127.0.0.1/%(param1)/%(_csv1)/%(_index0)", POST);
        template.setParams(Set.of(
                new TestParam("param1", Type.CONSTANT, null, "value1")
        ));

        var queue = new ExecutorQueue(template, CSV.from(DATA), Collections.emptyMap());
        List<HttpClient.Request> result = IteratorUtils.toList(queue);

        assertThat(result).hasSize(3);
        assertThat(queue.size()).isEqualTo(3);
        assertThat(result.get(0).uri().toString()).isEqualTo("http://127.0.0.1/value1/11/0");
        assertThat(result.get(1).uri().toString()).isEqualTo("http://127.0.0.1/value1/21/1");
        assertThat(result.get(2).uri().toString()).isEqualTo("http://127.0.0.1/value1/31/2");
    }

    @Test
    public void testBodyPlaceholdersReplaced() {
        var template = Template.create("test", "http://127.0.0.1", POST);
        template.setBody("%(param1);%(_csv1);%(_index0);%(unknown)");
        template.setParams(Set.of(
                new TestParam("param1", Type.CONSTANT, null, "value1")
        ));

        var queue = new ExecutorQueue(template, CSV.from(DATA), Collections.emptyMap());
        List<HttpClient.Request> result = IteratorUtils.toList(queue);

        assertThat(result).hasSize(3);
        assertThat(queue.size()).isEqualTo(3);
        assertThat(result.get(0).body()).isEqualTo("value1;11;0;%(unknown)");
        assertThat(result.get(1).body()).isEqualTo("value1;21;1;%(unknown)");
        assertThat(result.get(2).body()).isEqualTo("value1;31;2;%(unknown)");
    }

    @Test
    public void testBatchSizeWithoutRemainder() {
        var template = Template.create("test", "http://127.0.0.1", POST);
        template.setBody("%(_index0)");
        template.setBatch(new Batch(3, "[", "]", ";"));

        var queue = new ExecutorQueue(
                template,
                CSV.from(String.join("\n", DATA, DATA, DATA)),
                Collections.emptyMap()
        );
        List<HttpClient.Request> result = IteratorUtils.toList(queue);

        assertThat(result).hasSize(3);
        assertThat(queue.size()).isEqualTo(3);
        assertThat(result.get(0).body()).isEqualTo("[0;1;2]");
        assertThat(result.get(1).body()).isEqualTo("[3;4;5]");
        assertThat(result.get(2).body()).isEqualTo("[6;7;8]");
    }

    @Test
    public void testBatchSizeWithRemainder() {
        var template = Template.create("test", "http://127.0.0.1", POST);
        template.setBody("%(_index0)");
        template.setBatch(new Batch(4, "[", "]", ";"));

        var queue = new ExecutorQueue(
                template,
                CSV.from(String.join("\n", DATA, DATA)),
                Collections.emptyMap()
        );
        List<HttpClient.Request> result = IteratorUtils.toList(queue);

        assertThat(result).hasSize(2);
        assertThat(queue.size()).isEqualTo(2);
        assertThat(result.get(0).body()).isEqualTo("[0;1;2;3]");
        assertThat(result.get(1).body()).isEqualTo("[4;5]");
    }

    @Test
    public void testBatchOnlyParamPlaceholdersReplaced() {
        var template = Template.create("test", "http://127.0.0.1", POST);
        template.setBody("%(_index0)");
        template.setParams(Set.of(
                new TestParam("param1", Type.CONSTANT, null, "value1")
        ));
        template.setBatch(new Batch(
                3,
                "%(param1)-%(_csv1)-%(_index0)[",
                "]%(param1)-%(_csv1)-%(_index0)",
                ";")
        );
        var headers = Map.of(
                "header1", "%(param1)",
                "header2", "%(_csv1)",
                "header3", "%(_index0)"
        );

        var queue = new ExecutorQueue(template, CSV.from(DATA), headers);
        List<HttpClient.Request> result = IteratorUtils.toList(queue);

        assertThat(result).hasSize(1);
        assertThat(queue.size()).isEqualTo(1);
        assertThat(result.get(0).body()).isEqualTo("value1-%(_csv1)-%(_index0)[0;1;2]value1-%(_csv1)-%(_index0)");
        assertThat(result.get(0).headers()).isEqualTo(
                Map.of("header1", "value1", "header2", "%(_csv1)", "header3", "%(_index0)")
        );
    }

    @Test
    public void testAutoGeneratedParamsUpdatedAtEveryIteration() {
        var template = Template.create("test", "http://127.0.0.1", POST);
        template.setBody("%(param1) %(param2)");
        template.setParams(Set.of(
                new TestParam("param1", Type.PASSWORD, null, null),
                new TestParam("param2", Type.TIMESTAMP, null, null)
        ));

        var queue = new ExecutorQueue(template, CSV.from(DATA), Collections.emptyMap());
        List<HttpClient.Request> result = IteratorUtils.toList(queue);

        assertThat(result).hasSize(3);
        assertThat(queue.size()).isEqualTo(3);
        assertThat(result.get(0).body()).isEqualTo("password/0 timestamp/0");
        assertThat(result.get(1).body()).isEqualTo("password/1 timestamp/1");
        assertThat(result.get(2).body()).isEqualTo("password/2 timestamp/2");
    }

    @Test
    public void testAutoGeneratedParamsUpdatedAtEveryIterationBatchMode() {
        var template = Template.create("test", "http://127.0.0.1", POST);
        template.setBody("%(param1) %(param2)");
        template.setBatch(new Batch(2, "", "", "|")
        );
        template.setParams(Set.of(
                new TestParam("param1", Type.PASSWORD, null, null),
                new TestParam("param2", Type.TIMESTAMP, null, null)
        ));

        var queue = new ExecutorQueue(
                template,
                CSV.from(String.join("\n", DATA, DATA)),
                Collections.emptyMap()
        );
        List<HttpClient.Request> result = IteratorUtils.toList(queue);

        assertThat(result).hasSize(3);
        assertThat(queue.size()).isEqualTo(3);
        assertThat(result.get(0).body()).isEqualTo("password/0 timestamp/0|password/1 timestamp/1");
        assertThat(result.get(1).body()).isEqualTo("password/2 timestamp/2|password/3 timestamp/3");
        assertThat(result.get(2).body()).isEqualTo("password/4 timestamp/4|password/5 timestamp/5");
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class TestParam extends Param {

        private int counter = 0;

        public TestParam(String name, Type type, @Nullable String option, @Nullable String value) {
            super(name, type, option, value);
        }

        @Override
        public KeyValue<String, String> resolve() {
            var value = isAutoGenerated() ? (getType() + "/" + counter).toLowerCase() : getValue();
            var kv = new KeyValue<>(getName(), value);
            counter++;
            return kv;
        }
    }
}
