module.exports = {
  root: true,
  env: {
    browser: true,
    es6: true,
    node: true
  },
  extends: [
    '@dcloudio/uni-app'
  ],
  parserOptions: {
    ecmaVersion: 2020,
    sourceType: 'module'
  },
  rules: {
    // 生产环境禁用console
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'warn',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'warn',
    
    // Vue相关规则
    'vue/no-unused-vars': 'error',
    'vue/no-unused-components': 'warn',
    'vue/require-default-prop': 'error',
    'vue/require-prop-types': 'error',
    'vue/component-name-in-template-casing': ['error', 'kebab-case'],
    'vue/html-self-closing': ['error', {
      'html': {
        'void': 'never',
        'normal': 'always',
        'component': 'always'
      },
      'svg': 'always',
      'math': 'always'
    }],
    
    // JavaScript相关规则
    'no-unused-vars': ['error', { 
      'argsIgnorePattern': '^_',
      'varsIgnorePattern': '^_'
    }],
    'no-undef': 'error',
    'no-duplicate-imports': 'error',
    'no-var': 'error',
    'prefer-const': 'error',
    'prefer-arrow-callback': 'error',
    'arrow-spacing': 'error',
    'object-shorthand': 'error',
    'prefer-template': 'error',
    
    // 代码风格
    'indent': ['error', 2, { 'SwitchCase': 1 }],
    'quotes': ['error', 'single'],
    'semi': ['error', 'always'],
    'comma-dangle': ['error', 'never'],
    'space-before-function-paren': ['error', 'always'],
    'keyword-spacing': 'error',
    'space-infix-ops': 'error',
    'eol-last': 'error',
    'no-trailing-spaces': 'error',
    'no-multiple-empty-lines': ['error', { 'max': 2 }],
    
    // 最佳实践
    'eqeqeq': ['error', 'always'],
    'no-eval': 'error',
    'no-implied-eval': 'error',
    'no-new-func': 'error',
    'no-script-url': 'error',
    'no-alert': 'warn',
    'no-confirm': 'warn',
    'no-prompt': 'warn',
    
    // 性能相关
    'no-loop-func': 'error',
    'no-inner-declarations': 'error',
    
    // 安全相关
    'no-new-wrappers': 'error',
    'no-array-constructor': 'error',
    'no-new-object': 'error'
  },
  globals: {
    // uni-app全局变量
    'uni': 'readonly',
    'wx': 'readonly',
    'getApp': 'readonly',
    'getCurrentPages': 'readonly',
    'plus': 'readonly',
    'weex': 'readonly',
    'process': 'readonly'
  },
  overrides: [
    {
      files: ['*.vue'],
      rules: {
        'indent': 'off' // 让Vue文件使用自己的缩进规则
      }
    }
  ]
}
