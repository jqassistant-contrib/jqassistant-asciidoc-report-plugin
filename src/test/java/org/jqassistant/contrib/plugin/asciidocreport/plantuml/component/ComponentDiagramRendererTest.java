package org.jqassistant.contrib.plugin.asciidocreport.plantuml.component;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.graph.SubGraphFactory;
import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;

import org.jqassistant.contrib.plugin.asciidocreport.AbstractDiagramRendererTest;
import org.jqassistant.contrib.plugin.asciidocreport.plantuml.ImageRenderer;
import org.jqassistant.contrib.plugin.asciidocreport.plantuml.RenderMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link ImageRenderer}.
 */
@ExtendWith(MockitoExtension.class)
public class ComponentDiagramRendererTest extends AbstractDiagramRendererTest {

    @Mock
    private Result<?> result;

    @Mock
    private SubGraphFactory subGraphFactory;

    private ComponentDiagramRenderer componentDiagramRenderer;

    @BeforeEach
    public void setUp() {
        componentDiagramRenderer = new ComponentDiagramRenderer(subGraphFactory);
    }

    @Test
    public void componentDiagram() throws ReportException {
        doReturn(getSubGraph()).when(subGraphFactory).createSubGraph(result);

        String componentDiagram = componentDiagramRenderer.renderDiagram(result, RenderMode.GRAPHVIZ);

        assertThat(componentDiagram, containsString("[a1] <<Artifact File>> as n1"));
        assertThat(componentDiagram, containsString("[a2] <<Artifact File>> as n2"));
        assertThat(componentDiagram, containsString("[a3] <<Artifact File>> as n3"));
        assertThat(componentDiagram, containsString("n1 --> n2 : DEPENDS_ON"));
        assertThat(componentDiagram, not(containsString("[a4] <<Artifact File>> as n4")));
        assertThat(componentDiagram, not(containsString("n1 --> n4 : DEPENDS_ON")));
    }

    @Test
    public void jdotDiagram() throws ReportException {
        doReturn(getSubGraph()).when(subGraphFactory).createSubGraph(result);

        String componentDiagram = componentDiagramRenderer.renderDiagram(result, RenderMode.JDOT);

        assertThat(componentDiagram, containsString(RenderMode.JDOT.getPragma()));
    }

    @Test
    public void componentDiagramInFolder() throws ReportException {
        Node rootFolder = getNode(-1, "a0", "Artifact", "File", "Container");
        Node a1 = getNode(1, "a1", "Artifact", "File");
        SubGraph rootGraph = new SubGraph();
        rootGraph.setId(-1);
        rootGraph.getNodes().put(a1.getId(), a1);
        rootGraph.getNodes().put(rootFolder.getId(), rootFolder); // current behavior of SubGraphFactory
        rootGraph.setParent(rootFolder);
        Node nestedFolder = getNode(2, "a2", "Artifact", "File", "Container");
        Node a2 = getNode(3, "a3", "Artifact", "File");
        SubGraph nestedGraph = new SubGraph();
        nestedGraph.setId(-2);
        nestedGraph.setParent(nestedFolder);
        nestedGraph.getNodes().put(nestedFolder.getId(), nestedFolder);
        nestedGraph.getNodes().put(a2.getId(), a2);
        Relationship a2DependsOnA1 = getRelationship(1, a2, "DEPENDS_ON", a1);
        nestedGraph.getRelationships().put(a2DependsOnA1.getId(), a2DependsOnA1);
        rootGraph.getSubGraphs().put(nestedGraph.getId(), nestedGraph);

        doReturn(rootGraph).when(subGraphFactory).createSubGraph(result);

        String componentDiagram = componentDiagramRenderer.renderDiagram(result, RenderMode.GRAPHVIZ);

        assertThat(componentDiagram, containsString("folder \"a0\" {\n" + "    [a1] <<Artifact File>> as n1\n" + "    folder \"a2\" {\n"
                + "        [a3] <<Artifact File>> as n3\n" + "    }\n" + "}"));
        assertThat(componentDiagram, containsString("n3 --> n1 : DEPENDS_ON"));
    }

    private SubGraph getSubGraph() {
        Node a1 = getNode(1, "a1", "Artifact", "File");
        Node a2 = getNode(2, "a2", "Artifact", "File");
        Node a3 = getNode(3, "a3", "Artifact", "File");
        Node a4 = getNode(4, "a4", "Artifact", "File");
        Relationship a1DependsOnA2 = getRelationship(1, a1, "DEPENDS_ON", a2);
        Relationship a1DependsOnA4 = getRelationship(2, a1, "DEPENDS_ON", a4);
        SubGraph subGraph = new SubGraph();
        subGraph.getNodes().put(a1.getId(), a1);
        subGraph.getNodes().put(a2.getId(), a2);
        subGraph.getNodes().put(a3.getId(), a3);
        subGraph.getRelationships().put(a1DependsOnA2.getId(), a1DependsOnA2);
        subGraph.getRelationships().put(a1DependsOnA4.getId(), a1DependsOnA4);
        return subGraph;
    }
}