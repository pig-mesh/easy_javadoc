package com.star.easydoc.view.settings;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.*;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.star.easydoc.common.Consts;
import com.star.easydoc.config.EasyDocConfig;
import com.star.easydoc.config.EasyDocConfigComponent;
import com.star.easydoc.service.translator.TranslatorService;
import com.star.easydoc.view.inner.SupportView;
import com.star.easydoc.view.inner.WordMapAddView;
import com.star.easydoc.common.util.HttpUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.star.easydoc.service.RemoteWordMapService;

/**
 * @author wangchao
 * @date 2019/08/25
 */
public class CommonSettingsView {
    private static final Logger LOGGER = Logger.getInstance(CommonSettingsView.class);
    private TranslatorService translatorService = ServiceManager.getService(TranslatorService.class);
    private EasyDocConfig config = ServiceManager.getService(EasyDocConfigComponent.class).getState();

    private JPanel panel;
    private JPanel wordMapPanel;
    private JPanel commonPanel;
    private JComboBox<?> translatorBox;
    private JLabel translatorLabel;
    private JButton importButton;
    private JButton exportButton;
    private JTextField appIdTextField;
    private JTextField tokenTextField;
    private JButton resetButton;
    private JButton clearButton;
    private JLabel appIdLabel;
    private JLabel tokenLabel;
    private JTextField secretIdTextField;
    private JTextField secretKeyTextField;
    private JLabel secretIdLabel;
    private JLabel secretKeyLabel;
    private JButton starButton;
    private JButton payButton;
    private JTextField accessKeyIdTextField;
    private JTextField accessKeySecretTextField;
    private JLabel accessKeyIdLabel;
    private JLabel accessKeySecretLabel;
    private JPanel projectPanel;
    private JPanel projectListPanel;
    private JPanel projectWordMapPanel;
    private JButton reviewsButton;
    private JPanel supportPanel;
    private JTextField youdaoAppKeyTextField;
    private JTextField youdaoAppSecretTextField;
    private JLabel youdaoAppKeyLabel;
    private JLabel youdaoAppSecretLabel;
    private JTextField microsoftKeyTextField;
    private JTextField googleKeyTextField;
    private JLabel microsoftKeyLabel;
    private JLabel googleKeyLabel;
    private JTextField chatGlmApiKeyTextField;
    private JLabel chatGlmApiKeyLabel;
    private JTextField microsoftRegionTextField;
    private JLabel microsoftRegionLabel;
    private JTextField timeoutTextField;
    private JLabel timeoutLabel;
    private JTextField customUrlTextField;
    private JLabel customUrlLabel;
    private JButton customUrlHelpButton;
    private JLabel openaiBaseUrlLabel;
    private JTextField openaiBaseUrlTextField;
    private JLabel openaiApiKeyLabel;
    private JTextField openaiApiKeyTextField;
    private JLabel openaiModelLabel;
    private JTextField openaiModelTextField;
    private JLabel openaiTemperatureLabel;
    private JTextField openaiTemperatureTextField;
    private JLabel openaiTopKLabel;
    private JTextField openaiTopKTextField;
    private JButton openaiTestButton;
    private JBList<Entry<String, String>> typeMapList;
    private JBList<String> projectList;
    private JBList<Entry<String, String>> projectTypeMapList;

    // 远程单词映射URL相关的UI组件
    private JLabel remoteWordMapUrlLabel;
    private JTextField remoteWordMapUrlTextField;
    private JButton remoteWordMapTestButton;
    private JButton remoteWordMapImportButton;

    private static final String CUSTOM_HELP_URL
        = "https://github.com/starcwang/easy_javadoc/blob/master/doc/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E.md";

    /**
     * 晚于{@link #createUIComponents}执行
     */
    public CommonSettingsView() {
        refreshWordMap();
        refreshProjectWordMap();
        setVisible(translatorBox.getSelectedItem());

        importButton.addActionListener(event -> {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json");
            descriptor.setForcedToUseIdeaFileChooser(true);
            VirtualFile file = FileChooser.chooseFile(descriptor, null, null);
            if (file == null) {
                return;
            }
            if (!file.exists()) {
                LOGGER.error("file not exists:{}", file.getPath());
                return;
            }
            try {
                String json = FileUtils.readFileToString(new File(file.getPath()), StandardCharsets.UTF_8.name());
                EasyDocConfig configuration = JSON.parseObject(json, EasyDocConfig.class);
                if (configuration == null) {
                    throw new IllegalArgumentException("json error, please make sure the file format is json.");
                }
                ServiceManager.getService(EasyDocConfigComponent.class).loadState(configuration);
                refresh();
            } catch (Exception e) {
                LOGGER.error("read file error", e);
            }
        });

        exportButton.addActionListener(event -> {
            FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
            descriptor.setForcedToUseIdeaFileChooser(true);
            VirtualFile file = FileChooser.chooseFile(descriptor, null, null);
            if (file == null) {
                return;
            }
            if (!file.exists()) {
                LOGGER.error("folder not exists:{}", file.getPath());
                return;
            }
            try {
                File targetFile = new File(file.getPath() + "/easy_javadoc.json");
                FileUtils.write(targetFile, JSON.toJSONString(this.config, Feature.PrettyFormat), StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                LOGGER.error("write file error", e);
            }
        });

        resetButton.addActionListener(event -> {
            int result = JOptionPane.showConfirmDialog(null, "重置将删除所有配置，确认重置?", "确认", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                config.reset();
                refresh();
            }
        });

        clearButton.addActionListener(event -> {
            int result = JOptionPane.showConfirmDialog(null, "确认清空缓存?", "确认", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                translatorService.clearCache();
            }
        });

        starButton.addActionListener(event -> {
            try {
                Desktop dp = Desktop.getDesktop();
                if (dp.isSupported(Desktop.Action.BROWSE)) {
                    dp.browse(URI.create("https://github.com/starcwang/easy_javadoc"));
                }
            } catch (Exception e) {
                LOGGER.error("open url failed:https://github.com/starcwang/easy_javadoc", e);
            }
        });

        reviewsButton.addActionListener(event -> {
            try {
                Desktop dp = Desktop.getDesktop();
                if (dp.isSupported(Desktop.Action.BROWSE)) {
                    dp.browse(URI.create("https://plugins.jetbrains.com/plugin/12977-easy-javadoc/reviews"));
                }
            } catch (Exception e) {
                LOGGER.error("open url failed:https://plugins.jetbrains.com/plugin/12977-easy-javadoc/reviews", e);
            }
        });

        payButton.addActionListener(event -> {
            SupportView supportView = new SupportView();
            supportView.show();
        });

        translatorBox.addItemListener(e -> {
            JComboBox<?> jComboBox = (JComboBox<?>)e.getSource();
            setVisible(jComboBox.getSelectedItem());
        });

        customUrlHelpButton.addActionListener(event -> {
            Desktop dp = Desktop.getDesktop();
            if (dp.isSupported(Desktop.Action.BROWSE)) {
                try {
                    dp.browse(URI.create(CUSTOM_HELP_URL));
                } catch (IOException e) {
                    LOGGER.error("open url failed: " + CUSTOM_HELP_URL, e);
                }
            }
        });

        openaiTestButton.addActionListener(event -> testOpenAiConnection());

        projectList.addListSelectionListener(e -> refreshProjectWordMap());

        // 远程单词映射URL相关的事件监听器
        remoteWordMapTestButton.addActionListener(event -> testRemoteWordMapUrl());
        remoteWordMapImportButton.addActionListener(event -> importRemoteWordMap());
    }

    private void setVisible(Object selectedItem) {
        if (Consts.BAIDU_TRANSLATOR.equals(selectedItem)) {
            appIdLabel.setVisible(true);
            tokenLabel.setVisible(true);
            secretIdLabel.setVisible(false);
            secretKeyLabel.setVisible(false);
            accessKeyIdLabel.setVisible(false);
            accessKeySecretLabel.setVisible(false);
            youdaoAppKeyLabel.setVisible(false);
            youdaoAppSecretLabel.setVisible(false);
            microsoftKeyLabel.setVisible(false);
            microsoftRegionLabel.setVisible(false);
            googleKeyLabel.setVisible(false);
            chatGlmApiKeyLabel.setVisible(false);
            openaiBaseUrlLabel.setVisible(false);
            openaiApiKeyLabel.setVisible(false);
            openaiModelLabel.setVisible(false);
            openaiTemperatureLabel.setVisible(false);
            openaiTopKLabel.setVisible(false);
            customUrlLabel.setVisible(false);

            appIdTextField.setVisible(true);
            tokenTextField.setVisible(true);
            secretIdTextField.setVisible(false);
            secretKeyTextField.setVisible(false);
            accessKeyIdTextField.setVisible(false);
            accessKeySecretTextField.setVisible(false);
            youdaoAppKeyTextField.setVisible(false);
            youdaoAppSecretTextField.setVisible(false);
            microsoftKeyTextField.setVisible(false);
            microsoftRegionTextField.setVisible(false);
            googleKeyTextField.setVisible(false);
            chatGlmApiKeyTextField.setVisible(false);
            openaiBaseUrlTextField.setVisible(false);
            openaiApiKeyTextField.setVisible(false);
            openaiModelTextField.setVisible(false);
            openaiTemperatureTextField.setVisible(false);
            openaiTopKTextField.setVisible(false);
            customUrlTextField.setVisible(false);
            customUrlHelpButton.setVisible(false);
            openaiTestButton.setVisible(false);
        } else if (Consts.TENCENT_TRANSLATOR.equals(selectedItem)) {
            appIdLabel.setVisible(false);
            tokenLabel.setVisible(false);
            secretIdLabel.setVisible(true);
            secretKeyLabel.setVisible(true);
            accessKeyIdLabel.setVisible(false);
            accessKeySecretLabel.setVisible(false);
            youdaoAppKeyLabel.setVisible(false);
            youdaoAppSecretLabel.setVisible(false);
            microsoftKeyLabel.setVisible(false);
            microsoftRegionLabel.setVisible(false);
            googleKeyLabel.setVisible(false);
            chatGlmApiKeyLabel.setVisible(false);
            openaiBaseUrlLabel.setVisible(false);
            openaiApiKeyLabel.setVisible(false);
            openaiModelLabel.setVisible(false);
            openaiTemperatureLabel.setVisible(false);
            openaiTopKLabel.setVisible(false);
            customUrlLabel.setVisible(false);

            appIdTextField.setVisible(false);
            tokenTextField.setVisible(false);
            secretIdTextField.setVisible(true);
            secretKeyTextField.setVisible(true);
            accessKeyIdTextField.setVisible(false);
            accessKeySecretTextField.setVisible(false);
            youdaoAppKeyTextField.setVisible(false);
            youdaoAppSecretTextField.setVisible(false);
            microsoftKeyTextField.setVisible(false);
            microsoftRegionTextField.setVisible(false);
            googleKeyTextField.setVisible(false);
            chatGlmApiKeyTextField.setVisible(false);
            openaiBaseUrlTextField.setVisible(false);
            openaiApiKeyTextField.setVisible(false);
            openaiModelTextField.setVisible(false);
            openaiTemperatureTextField.setVisible(false);
            openaiTopKTextField.setVisible(false);
            customUrlTextField.setVisible(false);
            customUrlHelpButton.setVisible(false);
            openaiTestButton.setVisible(false);
        } else if (Consts.ALIYUN_TRANSLATOR.equals(selectedItem)) {
            appIdLabel.setVisible(false);
            tokenLabel.setVisible(false);
            secretIdLabel.setVisible(false);
            secretKeyLabel.setVisible(false);
            accessKeyIdLabel.setVisible(true);
            accessKeySecretLabel.setVisible(true);
            youdaoAppKeyLabel.setVisible(false);
            youdaoAppSecretLabel.setVisible(false);
            microsoftKeyLabel.setVisible(false);
            microsoftRegionLabel.setVisible(false);
            googleKeyLabel.setVisible(false);
            chatGlmApiKeyLabel.setVisible(false);
            openaiBaseUrlLabel.setVisible(false);
            openaiApiKeyLabel.setVisible(false);
            openaiModelLabel.setVisible(false);
            openaiTemperatureLabel.setVisible(false);
            openaiTopKLabel.setVisible(false);
            customUrlLabel.setVisible(false);

            appIdTextField.setVisible(false);
            tokenTextField.setVisible(false);
            secretIdTextField.setVisible(false);
            secretKeyTextField.setVisible(false);
            accessKeyIdTextField.setVisible(true);
            accessKeySecretTextField.setVisible(true);
            youdaoAppKeyTextField.setVisible(false);
            youdaoAppSecretTextField.setVisible(false);
            microsoftKeyTextField.setVisible(false);
            microsoftRegionTextField.setVisible(false);
            googleKeyTextField.setVisible(false);
            chatGlmApiKeyTextField.setVisible(false);
            openaiBaseUrlTextField.setVisible(false);
            openaiApiKeyTextField.setVisible(false);
            openaiModelTextField.setVisible(false);
            openaiTemperatureTextField.setVisible(false);
            openaiTopKTextField.setVisible(false);
            customUrlTextField.setVisible(false);
            customUrlHelpButton.setVisible(false);
            openaiTestButton.setVisible(false);
        } else if (Consts.YOUDAO_AI_TRANSLATOR.equals(selectedItem)) {
            appIdLabel.setVisible(false);
            tokenLabel.setVisible(false);
            secretIdLabel.setVisible(false);
            secretKeyLabel.setVisible(false);
            accessKeyIdLabel.setVisible(false);
            accessKeySecretLabel.setVisible(false);
            youdaoAppKeyLabel.setVisible(true);
            youdaoAppSecretLabel.setVisible(true);
            microsoftKeyLabel.setVisible(false);
            microsoftRegionLabel.setVisible(false);
            googleKeyLabel.setVisible(false);
            chatGlmApiKeyLabel.setVisible(false);
            openaiBaseUrlLabel.setVisible(false);
            openaiApiKeyLabel.setVisible(false);
            openaiModelLabel.setVisible(false);
            openaiTemperatureLabel.setVisible(false);
            openaiTopKLabel.setVisible(false);
            customUrlLabel.setVisible(false);

            appIdTextField.setVisible(false);
            tokenTextField.setVisible(false);
            secretIdTextField.setVisible(false);
            secretKeyTextField.setVisible(false);
            accessKeyIdTextField.setVisible(false);
            accessKeySecretTextField.setVisible(false);
            youdaoAppKeyTextField.setVisible(true);
            youdaoAppSecretTextField.setVisible(true);
            microsoftKeyTextField.setVisible(false);
            microsoftRegionTextField.setVisible(false);
            googleKeyTextField.setVisible(false);
            chatGlmApiKeyTextField.setVisible(false);
            openaiBaseUrlTextField.setVisible(false);
            openaiApiKeyTextField.setVisible(false);
            openaiModelTextField.setVisible(false);
            openaiTemperatureTextField.setVisible(false);
            openaiTopKTextField.setVisible(false);
            customUrlTextField.setVisible(false);
            customUrlHelpButton.setVisible(false);
            openaiTestButton.setVisible(false);
        } else if (Consts.MICROSOFT_TRANSLATOR.equals(selectedItem)) {
            appIdLabel.setVisible(false);
            tokenLabel.setVisible(false);
            secretIdLabel.setVisible(false);
            secretKeyLabel.setVisible(false);
            accessKeyIdLabel.setVisible(false);
            accessKeySecretLabel.setVisible(false);
            youdaoAppKeyLabel.setVisible(false);
            youdaoAppSecretLabel.setVisible(false);
            microsoftKeyLabel.setVisible(true);
            microsoftRegionLabel.setVisible(true);
            googleKeyLabel.setVisible(false);
            chatGlmApiKeyLabel.setVisible(false);
            openaiBaseUrlLabel.setVisible(false);
            openaiApiKeyLabel.setVisible(false);
            openaiModelLabel.setVisible(false);
            openaiTemperatureLabel.setVisible(false);
            openaiTopKLabel.setVisible(false);
            customUrlLabel.setVisible(false);

            appIdTextField.setVisible(false);
            tokenTextField.setVisible(false);
            secretIdTextField.setVisible(false);
            secretKeyTextField.setVisible(false);
            accessKeyIdTextField.setVisible(false);
            accessKeySecretTextField.setVisible(false);
            youdaoAppKeyTextField.setVisible(false);
            youdaoAppSecretTextField.setVisible(false);
            microsoftKeyTextField.setVisible(true);
            microsoftRegionTextField.setVisible(true);
            googleKeyTextField.setVisible(false);
            chatGlmApiKeyTextField.setVisible(false);
            openaiBaseUrlTextField.setVisible(false);
            openaiApiKeyTextField.setVisible(false);
            openaiModelTextField.setVisible(false);
            openaiTemperatureTextField.setVisible(false);
            openaiTopKTextField.setVisible(false);
            customUrlTextField.setVisible(false);
            customUrlHelpButton.setVisible(false);
            openaiTestButton.setVisible(false);
        } else if (Consts.GOOGLE_TRANSLATOR.equals(selectedItem)) {
            appIdLabel.setVisible(false);
            tokenLabel.setVisible(false);
            secretIdLabel.setVisible(false);
            secretKeyLabel.setVisible(false);
            accessKeyIdLabel.setVisible(false);
            accessKeySecretLabel.setVisible(false);
            youdaoAppKeyLabel.setVisible(false);
            youdaoAppSecretLabel.setVisible(false);
            microsoftKeyLabel.setVisible(false);
            microsoftRegionLabel.setVisible(false);
            googleKeyLabel.setVisible(true);
            chatGlmApiKeyLabel.setVisible(false);
            openaiBaseUrlLabel.setVisible(false);
            openaiApiKeyLabel.setVisible(false);
            openaiModelLabel.setVisible(false);
            openaiTemperatureLabel.setVisible(false);
            openaiTopKLabel.setVisible(false);
            customUrlLabel.setVisible(false);

            appIdTextField.setVisible(false);
            tokenTextField.setVisible(false);
            secretIdTextField.setVisible(false);
            secretKeyTextField.setVisible(false);
            accessKeyIdTextField.setVisible(false);
            accessKeySecretTextField.setVisible(false);
            youdaoAppKeyTextField.setVisible(false);
            youdaoAppSecretTextField.setVisible(false);
            microsoftKeyTextField.setVisible(false);
            microsoftRegionTextField.setVisible(false);
            googleKeyTextField.setVisible(true);
            chatGlmApiKeyTextField.setVisible(false);
            openaiBaseUrlTextField.setVisible(false);
            openaiApiKeyTextField.setVisible(false);
            openaiModelTextField.setVisible(false);
            openaiTemperatureTextField.setVisible(false);
            openaiTopKTextField.setVisible(false);
            customUrlTextField.setVisible(false);
            customUrlHelpButton.setVisible(false);
            openaiTestButton.setVisible(false);
        } else if (Consts.CHATGLM_GPT.equals(selectedItem)) {
            appIdLabel.setVisible(false);
            tokenLabel.setVisible(false);
            secretIdLabel.setVisible(false);
            secretKeyLabel.setVisible(false);
            accessKeyIdLabel.setVisible(false);
            accessKeySecretLabel.setVisible(false);
            youdaoAppKeyLabel.setVisible(false);
            youdaoAppSecretLabel.setVisible(false);
            microsoftKeyLabel.setVisible(false);
            googleKeyLabel.setVisible(false);
            chatGlmApiKeyLabel.setVisible(true);
            openaiBaseUrlLabel.setVisible(false);
            openaiApiKeyLabel.setVisible(false);
            openaiModelLabel.setVisible(false);
            openaiTemperatureLabel.setVisible(false);
            openaiTopKLabel.setVisible(false);
            customUrlLabel.setVisible(false);

            appIdTextField.setVisible(false);
            tokenTextField.setVisible(false);
            secretIdTextField.setVisible(false);
            secretKeyTextField.setVisible(false);
            accessKeyIdTextField.setVisible(false);
            accessKeySecretTextField.setVisible(false);
            youdaoAppKeyTextField.setVisible(false);
            youdaoAppSecretTextField.setVisible(false);
            microsoftKeyTextField.setVisible(false);
            googleKeyTextField.setVisible(false);
            chatGlmApiKeyTextField.setVisible(true);
            openaiBaseUrlTextField.setVisible(false);
            openaiApiKeyTextField.setVisible(false);
            openaiModelTextField.setVisible(false);
            openaiTemperatureTextField.setVisible(false);
            openaiTopKTextField.setVisible(false);
            customUrlTextField.setVisible(false);
            customUrlHelpButton.setVisible(false);
            openaiTestButton.setVisible(false);
        } else if (Consts.OPENAI_GPT.equals(selectedItem)) {
            appIdLabel.setVisible(false);
            tokenLabel.setVisible(false);
            secretIdLabel.setVisible(false);
            secretKeyLabel.setVisible(false);
            accessKeyIdLabel.setVisible(false);
            accessKeySecretLabel.setVisible(false);
            youdaoAppKeyLabel.setVisible(false);
            youdaoAppSecretLabel.setVisible(false);
            microsoftKeyLabel.setVisible(false);
            googleKeyLabel.setVisible(false);
            chatGlmApiKeyLabel.setVisible(false);
            openaiBaseUrlLabel.setVisible(true);
            openaiApiKeyLabel.setVisible(true);
            openaiModelLabel.setVisible(true);
            openaiTemperatureLabel.setVisible(true);
            openaiTopKLabel.setVisible(true);
            customUrlLabel.setVisible(false);

            appIdTextField.setVisible(false);
            tokenTextField.setVisible(false);
            secretIdTextField.setVisible(false);
            secretKeyTextField.setVisible(false);
            accessKeyIdTextField.setVisible(false);
            accessKeySecretTextField.setVisible(false);
            youdaoAppKeyTextField.setVisible(false);
            youdaoAppSecretTextField.setVisible(false);
            microsoftKeyTextField.setVisible(false);
            googleKeyTextField.setVisible(false);
            chatGlmApiKeyTextField.setVisible(false);
            openaiBaseUrlTextField.setVisible(true);
            openaiApiKeyTextField.setVisible(true);
            openaiModelTextField.setVisible(true);
            openaiTemperatureTextField.setVisible(true);
            openaiTopKTextField.setVisible(true);
            customUrlTextField.setVisible(false);
            customUrlHelpButton.setVisible(false);
            openaiTestButton.setVisible(true);
        } else if (Consts.CUSTOM_URL.equals(selectedItem)) {
            appIdLabel.setVisible(false);
            tokenLabel.setVisible(false);
            secretIdLabel.setVisible(false);
            secretKeyLabel.setVisible(false);
            accessKeyIdLabel.setVisible(false);
            accessKeySecretLabel.setVisible(false);
            youdaoAppKeyLabel.setVisible(false);
            youdaoAppSecretLabel.setVisible(false);
            microsoftKeyLabel.setVisible(false);
            googleKeyLabel.setVisible(false);
            chatGlmApiKeyLabel.setVisible(false);
            openaiBaseUrlLabel.setVisible(false);
            openaiApiKeyLabel.setVisible(false);
            openaiModelLabel.setVisible(false);
            openaiTemperatureLabel.setVisible(false);
            openaiTopKLabel.setVisible(false);
            customUrlLabel.setVisible(true);

            appIdTextField.setVisible(false);
            tokenTextField.setVisible(false);
            secretIdTextField.setVisible(false);
            secretKeyTextField.setVisible(false);
            accessKeyIdTextField.setVisible(false);
            accessKeySecretTextField.setVisible(false);
            youdaoAppKeyTextField.setVisible(false);
            youdaoAppSecretTextField.setVisible(false);
            microsoftKeyTextField.setVisible(false);
            googleKeyTextField.setVisible(false);
            chatGlmApiKeyTextField.setVisible(false);
            openaiBaseUrlTextField.setVisible(false);
            openaiApiKeyTextField.setVisible(false);
            openaiModelTextField.setVisible(false);
            openaiTemperatureTextField.setVisible(false);
            openaiTopKTextField.setVisible(false);
            customUrlTextField.setVisible(true);
            customUrlHelpButton.setVisible(true);
            openaiTestButton.setVisible(false);
        } else {
            appIdLabel.setVisible(false);
            tokenLabel.setVisible(false);
            secretIdLabel.setVisible(false);
            secretKeyLabel.setVisible(false);
            accessKeyIdLabel.setVisible(false);
            accessKeySecretLabel.setVisible(false);
            youdaoAppKeyLabel.setVisible(false);
            youdaoAppSecretLabel.setVisible(false);
            microsoftKeyLabel.setVisible(false);
            microsoftRegionLabel.setVisible(false);
            googleKeyLabel.setVisible(false);
            chatGlmApiKeyLabel.setVisible(false);
            openaiBaseUrlLabel.setVisible(false);
            openaiApiKeyLabel.setVisible(false);
            openaiModelLabel.setVisible(false);
            openaiTemperatureLabel.setVisible(false);
            openaiTopKLabel.setVisible(false);
            customUrlLabel.setVisible(false);

            appIdTextField.setVisible(false);
            tokenTextField.setVisible(false);
            secretIdTextField.setVisible(false);
            secretKeyTextField.setVisible(false);
            accessKeyIdTextField.setVisible(false);
            accessKeySecretTextField.setVisible(false);
            youdaoAppKeyTextField.setVisible(false);
            youdaoAppSecretTextField.setVisible(false);
            microsoftKeyTextField.setVisible(false);
            microsoftRegionTextField.setVisible(false);
            googleKeyTextField.setVisible(false);
            chatGlmApiKeyTextField.setVisible(false);
            openaiBaseUrlTextField.setVisible(false);
            openaiApiKeyTextField.setVisible(false);
            openaiModelTextField.setVisible(false);
            openaiTemperatureTextField.setVisible(false);
            openaiTopKTextField.setVisible(false);
            customUrlTextField.setVisible(false);
            customUrlHelpButton.setVisible(false);
            openaiTestButton.setVisible(false);
        }
    }

    /**
     * 早于构造方法{@link #CommonSettingsView}执行
     */
    private void createUIComponents() {
        config = ServiceManager.getService(EasyDocConfigComponent.class).getState();
        assert config != null;
        config.mergeProject();

        typeMapList = new JBList<>(new CollectionListModel<>(Lists.newArrayList()));
        typeMapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        typeMapList.setCellRenderer(new ListCellRendererWrapper<Entry<String, String>>() {
            @Override
            public void customize(JList list, Entry<String, String> value, int index, boolean selected, boolean hasFocus) {
                setText(value.getKey() + " -> " + value.getValue());
            }
        });

        typeMapList.setEmptyText("请添加单词映射");
        typeMapList.setSelectedIndex(0);
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(typeMapList);
        toolbarDecorator.setAddAction(button -> {
            WordMapAddView wordMapAddView = new WordMapAddView();
            if (wordMapAddView.showAndGet()) {
                Entry<String, String> entry = wordMapAddView.getMapping();
                config.getWordMap().put(entry.getKey(), entry.getValue());
                refreshWordMap();
            }
        });
        toolbarDecorator.disableUpDownActions();
        toolbarDecorator.setRemoveAction(anActionButton -> {
            Map<String, String> typeMap = config.getWordMap();
            typeMap.remove(typeMapList.getSelectedValue().getKey());
            refreshWordMap();
        });
        wordMapPanel = toolbarDecorator.createPanel();

        // 创建远程单词映射URL相关的UI组件
        remoteWordMapUrlLabel = new JLabel("远程词库:");
        remoteWordMapUrlTextField = new JTextField();
        remoteWordMapTestButton = new JButton("测试连接");
        remoteWordMapImportButton = new JButton("导入映射");

        // 将远程URL组件添加到wordMapPanel
        JPanel remoteUrlPanel = new JPanel(new BorderLayout());
        JPanel urlInputPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        urlInputPanel.add(remoteWordMapUrlLabel, BorderLayout.WEST);
        urlInputPanel.add(remoteWordMapUrlTextField, BorderLayout.CENTER);
        buttonPanel.add(remoteWordMapTestButton);
        buttonPanel.add(remoteWordMapImportButton);
        urlInputPanel.add(buttonPanel, BorderLayout.EAST);
        
        remoteUrlPanel.add(urlInputPanel, BorderLayout.NORTH);
        remoteUrlPanel.add(wordMapPanel, BorderLayout.CENTER);
        
        // 用包含远程URL组件的面板替换原来的wordMapPanel
        wordMapPanel = remoteUrlPanel;

        projectTypeMapList = new JBList<>(new CollectionListModel<>(Lists.newArrayList()));
        projectTypeMapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectTypeMapList.setCellRenderer(new ListCellRendererWrapper<Entry<String, String>>() {
            @Override
            public void customize(JList list, Entry<String, String> value, int index, boolean selected, boolean hasFocus) {
                setText(value.getKey() + " -> " + value.getValue());
            }
        });

        projectTypeMapList.setEmptyText("请添加单词映射");
        projectTypeMapList.setSelectedIndex(0);
        ToolbarDecorator projectWordToolbarDecorator = ToolbarDecorator.createDecorator(projectTypeMapList);
        projectWordToolbarDecorator.setAddAction(button -> {
            String projectName = projectList.getSelectedValue();
            if (projectName == null || projectName.isEmpty()) {
                return;
            }
            WordMapAddView wordMapAddView = new WordMapAddView();
            if (wordMapAddView.showAndGet()) {
                Entry<String, String> entry = wordMapAddView.getMapping();
                config.getProjectWordMap().computeIfAbsent(projectName, f -> Maps.newTreeMap())
                    .put(entry.getKey(), entry.getValue());
                refreshProjectWordMap();
            }
        });
        projectWordToolbarDecorator.disableUpDownActions();
        projectWordToolbarDecorator.setRemoveAction(anActionButton -> {
            Map<String, String> typeMap = config.getProjectWordMap().get(projectList.getSelectedValue());
            typeMap.remove(projectTypeMapList.getSelectedValue().getKey());
            refreshProjectWordMap();
        });
        projectWordMapPanel = projectWordToolbarDecorator.createPanel();

        projectList = new JBList<>(new CollectionListModel<>(Lists.newArrayList()));
        projectList.setModel(new CollectionListModel<>(config.getProjectWordMap().keySet()));
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectList.setCellRenderer(new ListCellRendererWrapper<String>() {
            @Override
            public void customize(JList list, String value, int index, boolean selected, boolean hasFocus) {
                setText(value);
            }
        });
        projectList.setSelectedIndex(0);

        ToolbarDecorator projectToolbarDecorator = ToolbarDecorator.createDecorator(projectList);
        projectToolbarDecorator.disableRemoveAction();
        projectToolbarDecorator.disableAddAction();
        projectToolbarDecorator.disableUpDownActions();
        projectListPanel = projectToolbarDecorator.createPanel();
    }

    public void refresh() {
        setTranslatorBox(config.getTranslator());
        setTimeout(config.getTimeout());
        setAppIdTextField(config.getAppId());
        setTokenTextField(config.getToken());
        setSecretIdTextField(config.getSecretId());
        setSecretKeyTextField(config.getSecretKey());
        setAccessKeyIdTextField(config.getAccessKeyId());
        setAccessKeySecretTextField(config.getAccessKeySecret());
        setYoudaoAppKeyTextField(config.getYoudaoAppKey());
        setYoudaoAppSecretTextField(config.getYoudaoAppSecret());
        setMicrosoftKeyTextField(config.getMicrosoftKey());
        setMicrosoftRegionTextField(config.getMicrosoftRegion());
        setGoogleKeyTextField(config.getGoogleKey());
        setChatGlmApiKeyTextField(config.getChatGlmApiKey());
        setOpenaiBaseUrlTextField(config.getOpenaiBaseUrl());
        setOpenaiApiKeyTextField(config.getOpenaiApiKey());
        setOpenaiModelTextField(config.getOpenaiModel());
        setOpenaiTemperatureTextField(config.getOpenaiTemperature() != null ? config.getOpenaiTemperature().toString() : "");
        setOpenaiTopKTextField(config.getOpenaiTopK() != null ? config.getOpenaiTopK().toString() : "");
        setCustomUrlTextField(config.getCustomUrl());
        setRemoteWordMapUrlTextField(config.getRemoteWordMapUrl());
        refreshWordMap();
        projectList.clearSelection();
        refreshProjectWordMap();
    }

    private void refreshWordMap() {
        if (null != config && config.getWordMap() != null) {
            typeMapList.setModel(new CollectionListModel<>(Lists.newArrayList(config.getWordMap().entrySet())));
        }
    }

    private void refreshProjectWordMap() {
        String projectName = projectList.getSelectedValue();
        SortedMap<String, TreeMap<String, String>> projectWordMap = config.getProjectWordMap();
        if (projectWordMap == null) {
            projectWordMap = Maps.newTreeMap();
        }

        // 没选择，默认页面
        if (projectName == null || projectName.isEmpty()) {
            projectList.setModel(new CollectionListModel<>(projectWordMap.keySet()));
            projectTypeMapList.setModel(new CollectionListModel<>(Lists.newArrayList()));
            return;
        }

        // 有选择，但配置中没有 -> 尝试初始化一次
        SortedMap<String, String> wordMap = projectWordMap.get(projectName);
        if (wordMap == null || wordMap.isEmpty()) {
            config.mergeProject();
        }
        wordMap = projectWordMap.get(projectName);
        // 还是没有
        if (wordMap == null) {
            wordMap = Maps.newTreeMap();
        }
        projectTypeMapList.setModel(new CollectionListModel<>(Lists.newArrayList(wordMap.entrySet())));
    }

    public JComboBox getTranslatorBox() {
        return translatorBox;
    }

    public JComponent getComponent() {
        return panel;
    }

    public void setTranslatorBox(String translator) {
        translatorBox.setSelectedItem(translator);
    }

    public JTextField getAppIdTextField() {
        return appIdTextField;
    }

    public void setAppIdTextField(String appId) {
        this.appIdTextField.setText(appId);
    }

    public JTextField getTokenTextField() {
        return tokenTextField;
    }

    public void setTokenTextField(String token) {
        this.tokenTextField.setText(token);
    }

    public JTextField getSecretIdTextField() {
        return secretIdTextField;
    }

    public void setSecretIdTextField(String secretId) {
        this.secretIdTextField.setText(secretId);
    }

    public JTextField getSecretKeyTextField() {
        return secretKeyTextField;
    }

    public void setSecretKeyTextField(String secretKey) {
        this.secretKeyTextField.setText(secretKey);
    }

    public JTextField getAccessKeyIdTextField() {
        return accessKeyIdTextField;
    }

    public void setAccessKeyIdTextField(String accessKeyId) {
        this.accessKeyIdTextField.setText(accessKeyId);
    }

    public JTextField getAccessKeySecretTextField() {
        return accessKeySecretTextField;
    }

    public void setAccessKeySecretTextField(String accessKeySecret) {
        this.accessKeySecretTextField.setText(accessKeySecret);
    }

    public JTextField getYoudaoAppKeyTextField() {
        return youdaoAppKeyTextField;
    }

    public JTextField getYoudaoAppSecretTextField() {
        return youdaoAppSecretTextField;
    }

    public void setYoudaoAppKeyTextField(String youdaoAppKey) {
        this.youdaoAppKeyTextField.setText(youdaoAppKey);
    }

    public void setYoudaoAppSecretTextField(String youdaoAppSecret) {
        this.youdaoAppSecretTextField.setText(youdaoAppSecret);
    }

    public JTextField getMicrosoftKeyTextField() {
        return microsoftKeyTextField;
    }

    public JTextField getMicrosoftRegionTextField() {
        return microsoftRegionTextField;
    }

    public void setMicrosoftKeyTextField(String microsoftKey) {
        this.microsoftKeyTextField.setText(microsoftKey);
    }

    public void setMicrosoftRegionTextField(String microsoftRegion) {
        this.microsoftRegionTextField.setText(microsoftRegion);
    }

    public JTextField getGoogleKeyTextField() {
        return googleKeyTextField;
    }

    public void setGoogleKeyTextField(String googleKey) {
        this.googleKeyTextField.setText(googleKey);
    }

    public JTextField getChatGlmApiKeyTextField() {
        return chatGlmApiKeyTextField;
    }

    public void setChatGlmApiKeyTextField(String apiKey) {
        this.chatGlmApiKeyTextField.setText(apiKey);
    }

    public JTextField getTimeoutTextField() {
        return timeoutTextField;
    }

    public void setTimeout(int timeout) {
        this.timeoutTextField.setText(String.valueOf(timeout));
    }

    public JTextField getCustomUrlTextField() {
        return customUrlTextField;
    }

    public void setCustomUrlTextField(String customUrl) {
        this.customUrlTextField.setText(customUrl);
    }

    public JTextField getOpenaiBaseUrlTextField() {
        return openaiBaseUrlTextField;
    }

    public void setOpenaiBaseUrlTextField(String openaiBaseUrl) {
        this.openaiBaseUrlTextField.setText(openaiBaseUrl);
    }

    public JTextField getOpenaiApiKeyTextField() {
        return openaiApiKeyTextField;
    }

    public void setOpenaiApiKeyTextField(String openaiApiKey) {
        this.openaiApiKeyTextField.setText(openaiApiKey);
    }

    public JTextField getOpenaiModelTextField() {
        return openaiModelTextField;
    }

    public void setOpenaiModelTextField(String openaiModel) {
        this.openaiModelTextField.setText(openaiModel);
    }

    public JTextField getOpenaiTemperatureTextField() {
        return openaiTemperatureTextField;
    }

    public void setOpenaiTemperatureTextField(String openaiTemperature) {
        this.openaiTemperatureTextField.setText(openaiTemperature);
    }

    public JTextField getOpenaiTopKTextField() {
        return openaiTopKTextField;
    }

    public void setOpenaiTopKTextField(String openaiTopK) {
        this.openaiTopKTextField.setText(openaiTopK);
    }

    public JTextField getRemoteWordMapUrlTextField() {
        return remoteWordMapUrlTextField;
    }

    public void setRemoteWordMapUrlTextField(String remoteWordMapUrl) {
        this.remoteWordMapUrlTextField.setText(remoteWordMapUrl);
    }

    /**
     * 测试远程单词映射URL是否可访问
     */
    private void testRemoteWordMapUrl() {
        String url = remoteWordMapUrlTextField.getText();
        if (url == null || url.trim().isEmpty()) {
            showTestResult("远程URL测试", "请先输入远程单词映射URL", false);
            return;
        }

        try {
            boolean isValid = RemoteWordMapService.validateRemoteUrl(url);
            if (isValid) {
                showTestResult("远程URL测试", "远程URL连接成功！", true);
            } else {
                showTestResult("远程URL测试", "远程URL连接失败，请检查URL是否正确", false);
            }
        } catch (Exception e) {
            showTestResult("远程URL测试", "测试失败：" + e.getMessage(), false);
        }
    }

    /**
     * 从远程URL导入单词映射
     */
    private void importRemoteWordMap() {
        String url = remoteWordMapUrlTextField.getText();
        if (url == null || url.trim().isEmpty()) {
            showTestResult("远程导入", "请先输入远程单词映射URL", false);
            return;
        }

        try {
            Map<String, String> remoteWordMap = RemoteWordMapService.fetchRemoteWordMap(url);
            if (remoteWordMap.isEmpty()) {
                showTestResult("远程导入", "未获取到任何单词映射", false);
                return;
            }

            // 将远程映射合并到本地全局映射中
            SortedMap<String, String> localWordMap = config.getWordMap();
            if (localWordMap == null) {
                localWordMap = Maps.newTreeMap();
            }

            int overwriteCount = 0;
            for (Map.Entry<String, String> entry : remoteWordMap.entrySet()) {
                if (localWordMap.containsKey(entry.getKey())) {
                    overwriteCount++;
                }
                localWordMap.put(entry.getKey(), entry.getValue());
            }

            config.setWordMap(localWordMap);
            refreshWordMap();

            String message = String.format("成功导入 %d 个单词映射", remoteWordMap.size());
            if (overwriteCount > 0) {
                message += String.format("，覆盖了 %d 个已存在的映射", overwriteCount);
            }
            showTestResult("远程导入", message, true);

        } catch (Exception e) {
            showTestResult("远程导入", "导入失败：" + e.getMessage(), false);
        }
    }

    private void testOpenAiConnection() {
        try {
            // 获取当前配置
            String baseUrl = openaiBaseUrlTextField.getText().trim();
            String apiKey = openaiApiKeyTextField.getText().trim();
            String model = openaiModelTextField.getText().trim();
            String temperatureText = openaiTemperatureTextField.getText().trim();
            String topKText = openaiTopKTextField.getText().trim();
            
            // 验证必填字段
            if (StringUtils.isBlank(baseUrl)) {
                showTestResult("❌ 测试失败", "Base URL不能为空", false);
                return;
            }
            if (StringUtils.isBlank(apiKey)) {
                showTestResult("❌ 测试失败", "API Key不能为空", false);
                return;
            }
            if (StringUtils.isBlank(model)) {
                model = "gpt-3.5-turbo";
            }
            
            // 确保URL格式正确
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }
            String url = baseUrl + "v1/chat/completions";
            
            // 构建请求头
            Map<String, String> headers = Maps.newHashMap();
            headers.put("Authorization", "Bearer " + apiKey);
            headers.put("Content-Type", "application/json");
            
            // 构建请求体
            Map<String, Object> message = Maps.newHashMap();
            message.put("role", "user");
            message.put("content", "test");
            
            Map<String, Object> request = Maps.newHashMap();
            request.put("model", model);
            request.put("messages", Lists.newArrayList(message));
            request.put("stream", false);
            request.put("max_tokens", 10);  // 限制token数量以节省费用
            
            // 设置温度参数
            if (StringUtils.isNotBlank(temperatureText)) {
                try {
                    double temperature = Double.parseDouble(temperatureText);
                    if (temperature >= 0.0 && temperature <= 2.0) {
                        request.put("temperature", temperature);
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效的温度值
                }
            }
            
            // 设置top_p参数（从TopK转换）
            if (StringUtils.isNotBlank(topKText)) {
                try {
                    int topK = Integer.parseInt(topKText);
                    if (topK > 0 && topK <= 100) {
                        double topP = Math.min(1.0, Math.max(0.0, topK / 100.0));
                        if (topP > 0.0) {
                            request.put("top_p", topP);
                        }
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效的TopK值
                }
            }
            
            String requestJson = JSON.toJSONString(request);
            
            // 发送请求
            String response = HttpUtil.postJson(url, headers, requestJson, 10000);
            
            if (StringUtils.isBlank(response)) {
                showTestResult("❌ 测试失败", "API响应为空", false);
                return;
            }
            
            // 解析响应
            com.alibaba.fastjson2.JSONObject jsonResponse = JSON.parseObject(response);
            if (jsonResponse == null) {
                showTestResult("❌ 测试失败", "无法解析API响应: " + response, false);
                return;
            }
            
            // 检查是否有错误
            if (jsonResponse.containsKey("error")) {
                com.alibaba.fastjson2.JSONObject error = jsonResponse.getJSONObject("error");
                String errorMessage = error.getString("message");
                String errorType = error.getString("type");
                String errorCode = error.getString("code");
                
                String fullError = "API错误: " + errorType;
                if (StringUtils.isNotBlank(errorCode)) {
                    fullError += " (代码: " + errorCode + ")";
                }
                fullError += "\n" + errorMessage;
                
                showTestResult("❌ 测试失败", fullError, false);
                return;
            }
            
            // 检查成功响应
            if (jsonResponse.containsKey("choices")) {
                showTestResult("✅ 测试成功", "OpenAI API连接正常!\n\n完整响应:\n" + JSON.toJSONString(jsonResponse, com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat), true);
            } else {
                showTestResult("⚠️ 测试异常", "API响应格式异常:\n" + JSON.toJSONString(jsonResponse, com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat), false);
            }
            
        } catch (Exception e) {
            String errorMessage = "连接失败: " + e.getMessage();
            if (e.getMessage().contains("UnknownHostException")) {
                errorMessage = "网络连接失败，请检查Base URL是否正确";
            } else if (e.getMessage().contains("ConnectTimeoutException")) {
                errorMessage = "连接超时，请检查网络连接和Base URL";
            } else if (e.getMessage().contains("401")) {
                errorMessage = "API Key无效，请检查API Key是否正确";
            } else if (e.getMessage().contains("403")) {
                errorMessage = "API访问被拒绝，请检查API Key权限";
            } else if (e.getMessage().contains("404")) {
                errorMessage = "API端点不存在，请检查Base URL是否正确";
            }
            showTestResult("❌ 测试失败", errorMessage, false);
        }
    }
    
    private void showTestResult(String title, String message, boolean isSuccess) {
        // 根据成功或失败显示不同的图标和样式
        int messageType = isSuccess ? javax.swing.JOptionPane.INFORMATION_MESSAGE : javax.swing.JOptionPane.ERROR_MESSAGE;
        
        // 创建一个可滚动的文本区域来显示长消息
        javax.swing.JTextArea textArea = new javax.swing.JTextArea(15, 50);
        textArea.setText(message);
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        javax.swing.JOptionPane.showMessageDialog(
            this.panel,
            scrollPane,
            title,
            messageType
        );
    }
}
