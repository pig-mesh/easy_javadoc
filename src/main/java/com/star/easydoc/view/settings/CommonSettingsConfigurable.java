package com.star.easydoc.view.settings;

import java.util.Objects;
import java.util.TreeMap;

import javax.swing.*;

import com.google.common.collect.Maps;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.star.easydoc.common.Consts;
import com.star.easydoc.config.EasyDocConfig;
import com.star.easydoc.config.EasyDocConfigComponent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

/**
 * 通用设置 可配置
 *
 * @author wangchao
 * @date 2019/08/25
 */
public class CommonSettingsConfigurable implements Configurable {

    /** 配置 */
    private EasyDocConfig config = ServiceManager.getService(EasyDocConfigComponent.class).getState();
    /** 视图 */
    private CommonSettingsView view = new CommonSettingsView();

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "EasyDoc";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return view.getComponent();
    }

    @Override
    public boolean isModified() {
        if (!Objects.equals(config.getTranslator(), view.getTranslatorBox().getSelectedItem())) {
            return true;
        }
        if (!Objects.equals(String.valueOf(config.getTimeout()), view.getTimeoutTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getAppId(), view.getAppIdTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getToken(), view.getTokenTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getSecretKey(), view.getSecretKeyTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getSecretId(), view.getSecretIdTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getAccessKeyId(), view.getAccessKeyIdTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getAccessKeySecret(), view.getAccessKeySecretTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getYoudaoAppKey(), view.getYoudaoAppKeyTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getYoudaoAppSecret(), view.getYoudaoAppSecretTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getMicrosoftKey(), view.getMicrosoftKeyTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getMicrosoftRegion(), view.getMicrosoftRegionTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getGoogleKey(), view.getGoogleKeyTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getChatGlmApiKey(), view.getChatGlmApiKeyTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getOpenaiBaseUrl(), view.getOpenaiBaseUrlTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getOpenaiApiKey(), view.getOpenaiApiKeyTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getOpenaiModel(), view.getOpenaiModelTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getOpenaiTemperature() != null ? config.getOpenaiTemperature().toString() : "", view.getOpenaiTemperatureTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getOpenaiTopK() != null ? config.getOpenaiTopK().toString() : "", view.getOpenaiTopKTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getCustomUrl(), view.getCustomUrlTextField().getText())) {
            return true;
        }
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        config.setTranslator(String.valueOf(view.getTranslatorBox().getSelectedItem()));
        config.setAppId(view.getAppIdTextField().getText());
        config.setToken(view.getTokenTextField().getText());
        config.setSecretKey(view.getSecretKeyTextField().getText());
        config.setSecretId(view.getSecretIdTextField().getText());
        config.setAccessKeyId(view.getAccessKeyIdTextField().getText());
        config.setAccessKeySecret(view.getAccessKeySecretTextField().getText());
        config.setYoudaoAppKey(view.getYoudaoAppKeyTextField().getText());
        config.setYoudaoAppSecret(view.getYoudaoAppSecretTextField().getText());
        config.setMicrosoftKey(view.getMicrosoftKeyTextField().getText());
        config.setMicrosoftRegion(view.getMicrosoftRegionTextField().getText());
        config.setGoogleKey(view.getGoogleKeyTextField().getText());
        config.setChatGlmApiKey(view.getChatGlmApiKeyTextField().getText());
        config.setOpenaiBaseUrl(view.getOpenaiBaseUrlTextField().getText());
        config.setOpenaiApiKey(view.getOpenaiApiKeyTextField().getText());
        config.setOpenaiModel(view.getOpenaiModelTextField().getText());
        config.setOpenaiTemperature(view.getOpenaiTemperatureTextField().getText() != null ? Double.parseDouble(view.getOpenaiTemperatureTextField().getText()) : null);
        config.setOpenaiTopK(view.getOpenaiTopKTextField().getText() != null ? Integer.parseInt(view.getOpenaiTopKTextField().getText()) : null);
        config.setCustomUrl(StringUtils.strip(view.getCustomUrlTextField().getText()));
        if (config.getWordMap() == null) {
            config.setWordMap(new TreeMap<>());
        }
        if (config.getProjectWordMap() == null) {
            config.setProjectWordMap(Maps.newTreeMap());
        }

        if (config.getTranslator() == null || !Consts.ENABLE_TRANSLATOR_SET.contains(config.getTranslator())) {
            throw new ConfigurationException("请选择正确的翻译方式");
        }
        if (Consts.BAIDU_TRANSLATOR.equals(config.getTranslator())) {
            if (StringUtils.isBlank(config.getAppId())) {
                throw new ConfigurationException("appId不能为空");
            }
            if (StringUtils.isBlank(config.getToken())) {
                throw new ConfigurationException("密钥不能为空");
            }
        }
        if (Consts.TENCENT_TRANSLATOR.equals(config.getTranslator())) {
            if (StringUtils.isBlank(config.getSecretKey())) {
                throw new ConfigurationException("secretKey不能为空");
            }
            if (StringUtils.isBlank(config.getSecretId())) {
                throw new ConfigurationException("secretId不能为空");
            }
        }
        if (Consts.ALIYUN_TRANSLATOR.equals(config.getTranslator())) {
            if (StringUtils.isBlank(config.getAccessKeyId())) {
                throw new ConfigurationException("accessKeyId不能为空");
            }
            if (StringUtils.isBlank(config.getAccessKeySecret())) {
                throw new ConfigurationException("accessKeySecret不能为空");
            }
        }
        if (Consts.YOUDAO_AI_TRANSLATOR.equals(config.getTranslator())) {
            if (StringUtils.isBlank(config.getYoudaoAppKey())) {
                throw new ConfigurationException("appKey不能为空");
            }
            if (StringUtils.isBlank(config.getYoudaoAppSecret())) {
                throw new ConfigurationException("appSecret不能为空");
            }
        }
        if (Consts.MICROSOFT_TRANSLATOR.equals(config.getTranslator())) {
            if (StringUtils.isBlank(config.getMicrosoftKey())) {
                throw new ConfigurationException("microsoftKey不能为空");
            }
        }
        if (Consts.GOOGLE_TRANSLATOR.equals(config.getTranslator())) {
            if (StringUtils.isBlank(config.getGoogleKey())) {
                throw new ConfigurationException("googleKey不能为空");
            }
        }
        if (Consts.CHATGLM_GPT.equals(config.getTranslator())) {
            if (StringUtils.isBlank(config.getChatGlmApiKey())) {
                throw new ConfigurationException("apiKey不能为空");
            }
        }
        if (Consts.OPENAI_GPT.equals(config.getTranslator())) {
            if (StringUtils.isBlank(config.getOpenaiApiKey())) {
                throw new ConfigurationException("OpenAI API Key不能为空");
            }
            if (StringUtils.isBlank(config.getOpenaiBaseUrl())) {
                throw new ConfigurationException("OpenAI Base URL不能为空");
            }
            if (StringUtils.isBlank(config.getOpenaiModel())) {
                throw new ConfigurationException("OpenAI Model不能为空");
            }
            try {
                if (StringUtils.isNotBlank(view.getOpenaiTemperatureTextField().getText())) {
                    double temperature = Double.parseDouble(view.getOpenaiTemperatureTextField().getText());
                    if (temperature < 0.0 || temperature > 2.0) {
                        throw new ConfigurationException("OpenAI Temperature参数必须在0.0-2.0之间");
                    }
                }
            } catch (NumberFormatException e) {
                throw new ConfigurationException("OpenAI Temperature参数必须是有效的数字");
            }
            try {
                if (StringUtils.isNotBlank(view.getOpenaiTopKTextField().getText())) {
                    int topK = Integer.parseInt(view.getOpenaiTopKTextField().getText());
                    if (topK < 1 || topK > 100) {
                        throw new ConfigurationException("OpenAI TopK参数必须在1-100之间");
                    }
                }
            } catch (NumberFormatException e) {
                throw new ConfigurationException("OpenAI TopK参数必须是有效的整数");
            }
        }
        if (Consts.CUSTOM_URL.equals(config.getTranslator())) {
            if (StringUtils.isBlank(config.getCustomUrl())) {
                throw new ConfigurationException("自定义地址不能为空");
            }
            if (!config.getCustomUrl().startsWith("http")) {
                throw new ConfigurationException("自定义地址只支持http或https接口");
            }
            if (!config.getCustomUrl().contains("{from}")) {
                throw new ConfigurationException("自定义地址需要包含{from}占位符，请查看说明文档");
            }
            if (!config.getCustomUrl().contains("{to}")) {
                throw new ConfigurationException("自定义地址需要包含{to}占位符，请查看说明文档");
            }
            if (!config.getCustomUrl().contains("{query}")) {
                throw new ConfigurationException("自定义地址需要包含{query}占位符，请查看说明文档");
            }
        }
        if (StringUtils.isBlank(view.getTimeoutTextField().getText())
            || !view.getTimeoutTextField().getText().matches("^[1-9][0-9]*$")) {
            throw new ConfigurationException("超时时间必须为数字");
        }
        config.setTimeout(Integer.parseInt(view.getTimeoutTextField().getText()));
    }

    @Override
    public void reset() {
        view.refresh();
    }
}
