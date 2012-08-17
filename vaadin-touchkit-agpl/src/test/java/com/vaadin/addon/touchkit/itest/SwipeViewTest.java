package com.vaadin.addon.touchkit.itest;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Ignore;

import com.vaadin.Application;
import com.vaadin.addon.touchkit.AbstractTouchKitIntegrationTest;
import com.vaadin.addon.touchkit.ui.NavigationBar;
import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.NavigationManager;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationEvent.Direction;
import com.vaadin.addon.touchkit.ui.SwipeView;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.external.org.apache.commons.io.IOUtils;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.RequestHandler;
import com.vaadin.terminal.WrappedRequest;
import com.vaadin.terminal.WrappedResponse;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
@Ignore
public class SwipeViewTest extends AbstractTouchKitIntegrationTest {

    public SwipeViewTest() {
        addComponent(new SwipeViewTestMgr());
    }

    @Override
    public void attach() {
        super.attach();

        getApplication().addRequestHandler(new RequestHandler() {

            @Override
            public boolean handleRequest(Application application,
                    WrappedRequest request, WrappedResponse response)
                    throws IOException {
                String requestPathInfo = request.getRequestPathInfo();
                if (requestPathInfo.contains("winterphotos/")) {
                    response.setCacheTime(60 * 60 * 1000);
                    response.setContentType("image/jpeg");

                    String ss = requestPathInfo.substring(requestPathInfo
                            .lastIndexOf("/") + 1);
                    InputStream resourceAsStream = getClass()
                            .getResourceAsStream("/winterphotos/" + ss);
                    IOUtils.copy(resourceAsStream, response.getOutputStream());
                    return true;
                }
                return false;
            }
        });
    }

    public static class SwipeViewTestMgr extends NavigationManager {

        int index = 0;

        boolean loop = true;

        SwipeView[] images;

        public SwipeViewTestMgr() {
            setMaintainBreadcrumb(false);
            setWidth("100%");
            images = loadImages();

            SwipeView prev = getImage(index - 1);
            SwipeView cur = getImage(index);
            SwipeView next = getImage(index + 1);
            setPreviousComponent(prev);
            setCurrentComponent(cur);
            setNextComponent(next);
            updateNextPreviousInCurrentCompoenent();

            addListener(new NavigationListener() {
                public void navigate(NavigationEvent event) {
                    if (event.getDirection() == Direction.FORWARD) {
                        index++;
                        int nextViewIndex = index + 1;
                        while (nextViewIndex >= images.length) {
                            nextViewIndex -= images.length;
                        }
                        if (loop || nextViewIndex != 0) {
                            SwipeView next = getImage(nextViewIndex);
                            setNextComponent(next);
                        }
                    } else {
                        index--;
                        int i = index - 1;
                        while (i < 0) {
                            i += images.length;
                        }
                        if (loop || i != images.length - 1) {
                            SwipeView prev = getImage(i);
                            setPreviousComponent(prev);
                        }

                    }
                    updateNextPreviousInCurrentCompoenent();
                }
            });

        }

        private void updateNextPreviousInCurrentCompoenent() {
            Component currentComponent2 = getCurrentComponent();

            if (currentComponent2 instanceof ImageView) {
                ImageView new_name = (ImageView) currentComponent2;
                NavigationButton leftComponent = (NavigationButton) new_name.navigationBar
                        .getLeftComponent();
                leftComponent.setTargetView(getPreviousComponent());
                NavigationButton rightComponent = (NavigationButton) new_name.navigationBar
                        .getRightComponent();
                rightComponent.setTargetView(getNextComponent());
            }
        }

        private SwipeView getImage(int i) {
            while (i < 0) {
                i += images.length;
            }
            return images[i % images.length];
        }

        private SwipeView[] loadImages() {
            String[] filenames = new String[] { "Peimari during winter.jpg",
                    "Peimari, another skier.jpg",
                    "Perfect sunshine on Peimari ice.jpg",
                    "Sanders_fished_from_Peimari_.jpg",
                    "Snow_trees_and_sunshine_ in_Trysil.jpg",
                    "Snowy view in Trysil.jpg", "Sunset in Trysil.jpg",
                    "Swamp in Trysil during the winter.jpg",
                    "Track and shadow in powder snow.jpg",
                    "Trysil, break before reaching the peak.jpg",
                    "View to south on Peimari ice.jpg" };

            SwipeView[] ss = new SwipeView[filenames.length + 1];

            for (int i = 0; i < filenames.length; i++) {
                final String f = filenames[i];
                ss[i] = new ImageView(f);
            }
            ss[ss.length - 1] = new SwipeViewWithTabsheet();

            return ss;
        }

        class SwipeViewWithTabsheet extends SwipeView {
            public SwipeViewWithTabsheet() {
                setMargin(true);
                Form form = new Form();

                CheckBox cb = new CheckBox();

                cb.setCaption("Loop views");
                cb.addListener(new Property.ValueChangeListener() {
                    public void valueChange(ValueChangeEvent event) {
                        loop = !loop;
                        if (loop) {
                            setNextComponent(images[0]);
                        } else {
                            setNextComponent(null);
                        }
                    }
                });
                cb.setImmediate(true);

                form.addField("loop", cb);

                TextField tf = new TextField("Foo");
                tf.setWidth("100%");
                tf.setValue("This is a test page that shows SwipeView can also contain other stuff but just images :-)");
                form.addField("foo", tf);
                form.addField("bar", new CheckBox("Bar"));
                NativeSelect field = new NativeSelect();
                field.setCaption("Car");
                field.addItem("Foo");
                field.addItem("Bar");
                field.addItem("Car");
                form.addField("car", field);

                VerticalComponentGroup fg = new VerticalComponentGroup();
                fg.setCaption("Form");
                fg.addComponent(form);
                addComponent(fg);

                VerticalComponentGroup verticalComponentGroup = new VerticalComponentGroup();
                verticalComponentGroup
                        .setCaption("Labels to make this view heavy");

                for (int i = 0; i < 40; i++) {
                    verticalComponentGroup
                            .addComponent(new Label("Label " + i));
                }

                addComponent(verticalComponentGroup);

            }
        }

        class ImageView extends SwipeView {

            private String ss;
            private Embedded embedded = new Embedded();
            private NavigationBar navigationBar;

            public ImageView(String f) {
                setWidth("100%");
                ss = f;
                navigationBar = new NavigationBar();
                NavigationButton button = new NavigationButton("<");
                button.setStyleName("back");
                navigationBar.setLeftComponent(button);
                navigationBar.setCaption(ss.replace(".jpg", "").replace("_",
                        " "));
                button = new NavigationButton(">");
                navigationBar.setRightComponent(button);
                // FIXME (add back when NavigationBar works.
//                addComponent(navigationBar);
                button.setStyleName("forward");
                embedded.setWidth("100%");
                addComponent(embedded);
            }

            @Override
            protected String getCss(Component c) {
                if (c == navigationBar) {
                    // Make background of bar semitranparent over the image.
                    return "background: rgba(255, 255, 255, 0.7); position:absolute;top:0;left:0;right:0;";
                }
                return super.getCss(c);
            }

            @Override
            public void attach() {
                super.attach();

                Application application = getApplication();
                if (application == null) {
                    throw new RuntimeException("WTF!!");
                }
                ExternalResource source = new ExternalResource(getApplication()
                        .getURL() +"winterphotos/"+ ss);
                embedded.setSource(source);
            }

            @Override
            public void detach() {
                super.detach();
            }
        }

    }
}