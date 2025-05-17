# ai_model/download_models.py
"""
Mistral-7B + LoRA 모델 다운로드 및 설정 스크립트
"""
import os
import sys
import gc
from pathlib import Path
from huggingface_hub import snapshot_download, login
from transformers import AutoTokenizer, AutoModelForCausalLM, BitsAndBytesConfig
from peft import PeftModel
import torch
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class MistralLoRADownloader:
    def __init__(self):
        self.hf_token = os.getenv('HF_TOKEN')
        # 환경변수 또는 기본값 설정
        self.model_name = os.getenv('BASE_MODEL_NAME', 'mistralai/Mistral-7B-v0.1')
        self.base_model_path = os.getenv('BASE_MODEL_PATH', '/srv/models/base')
        self.adapter_path = os.getenv('ADAPTER_PATH', '/srv/models/mistral_lora_adapter')
        self.cache_dir = os.getenv('HF_HOME', '/srv/models/cache')
        
        # 경로 생성
        os.makedirs(self.base_model_path, exist_ok=True)
        os.makedirs(os.path.dirname(self.adapter_path), exist_ok=True)
        os.makedirs(self.cache_dir, exist_ok=True)
        
        # 8bit 양자화 설정 (참고 코드에 맞게)
        self.bnb_config = BitsAndBytesConfig(
            load_in_8bit=True,
            bnb_8bit_compute_dtype=torch.float16,
            bnb_8bit_use_double_nested_quant=True  # QLoRA 식 2-레벨 양자화
        )
    
    def login_huggingface(self):
        """허깅페이스 로그인"""
        if self.hf_token:
            login(token=self.hf_token)
            logger.info("허깅페이스 로그인 성공")
        else:
            logger.warning("허깅페이스 토큰이 없습니다.")
    
    def download_base_model(self):
        """Mistral-7B 베이스 모델 다운로드"""
        logger.info(f"Mistral-7B 모델 다운로드 시작: {self.model_name}")
        
        try:
            # 모델 다운로드
            snapshot_download(
                repo_id=self.model_name,
                local_dir=self.base_model_path,
                local_dir_use_symlinks=False,
                ignore_patterns=["*.git*", "README.md", "*.txt"],
                cache_dir=self.cache_dir
            )
            
            # 토크나이저 다운로드 및 저장
            tokenizer = AutoTokenizer.from_pretrained(
                self.model_name,
                cache_dir=self.cache_dir
            )
            tokenizer.save_pretrained(self.base_model_path)
            
            # 패딩 토큰 설정 추가 저장
            tokenizer.pad_token = tokenizer.eos_token
            tokenizer.save_pretrained(self.base_model_path)
            
            logger.info(f"Mistral-7B 다운로드 완료: {self.base_model_path}")
            
        except Exception as e:
            logger.error(f"모델 다운로드 실패: {str(e)}")
            raise
    
    def check_storage_space(self):
        """저장 공간 확인 (Mistral-7B는 약 14GB)"""
        statvfs = os.statvfs(self.base_model_path)
        free_space = statvfs.f_frsize * statvfs.f_bavail
        free_gb = free_space / (1024**3)
        
        logger.info(f"사용 가능한 저장 공간: {free_gb:.2f} GB")
        
        # Mistral-7B + 8bit 양자화를 위해 최소 20GB 필요
        if free_gb < 20:
            logger.warning("저장 공간이 부족합니다! (최소 20GB 필요)")
            return False
        return True
    
    def check_gpu_availability(self):
        """GPU 사용 가능 여부 확인"""
        if torch.cuda.is_available():
            gpu_count = torch.cuda.device_count()
            logger.info(f"GPU {gpu_count}개 감지됨")
            for i in range(gpu_count):
                gpu_name = torch.cuda.get_device_name(i)
                logger.info(f"GPU {i}: {gpu_name}")
            return True
        else:
            logger.warning("GPU를 찾을 수 없습니다. CPU 모드로 실행됩니다.")
            return False
    
    def check_existing_adapter(self):
        """기존 LoRA 어댑터 확인"""
        if not os.path.exists(self.adapter_path):
            logger.warning(f"어댑터 경로가 존재하지 않습니다: {self.adapter_path}")
            return False
        
        # LoRA 어댑터 필수 파일 확인
        required_files = ['adapter_config.json', 'adapter_model.bin']
        for file in required_files:
            file_path = os.path.join(self.adapter_path, file)
            if not os.path.exists(file_path):
                logger.warning(f"필수 LoRA 파일 누락: {file}")
                return False
        
        logger.info(f"LoRA 어댑터 확인됨: {self.adapter_path}")
        return True
    
    def test_model_loading(self):
        """모델 로딩 테스트 (선택사항)"""
        if self.check_gpu_availability():
            logger.info("모델 로딩 테스트 시작...")
            try:
                # 베이스 모델 로드 테스트
                base_model = AutoModelForCausalLM.from_pretrained(
                    self.base_model_path,
                    quantization_config=self.bnb_config,
                    device_map="auto"
                )
                
                # LoRA 어댑터가 있으면 로드 테스트
                if self.check_existing_adapter():
                    model = PeftModel.from_pretrained(base_model, self.adapter_path)
                    model.eval()
                    logger.info("LoRA 모델 로딩 성공")
                
                # 메모리 정리
                del base_model
                if 'model' in locals():
                    del model
                gc.collect()
                torch.cuda.empty_cache()
                
                logger.info("모델 로딩 테스트 완료")
                return True
            except Exception as e:
                logger.error(f"모델 로딩 테스트 실패: {str(e)}")
                return False
        return True
    
    def download_all_models(self):
        """전체 다운로드 프로세스"""
        logger.info("Mistral-7B + LoRA 다운로드 프로세스 시작")
        
        # 저장 공간 확인
        if not self.check_storage_space():
            raise ValueError("저장 공간이 부족합니다!")
        
        # GPU 체크
        self.check_gpu_availability()
        
        # 허깅페이스 로그인
        self.login_huggingface()
        
        # 베이스 모델 다운로드
        self.download_base_model()
        
        # 어댑터 확인
        self.check_existing_adapter()
        
        # 선택사항: 모델 로딩 테스트
        test_loading = os.getenv('TEST_MODEL_LOADING', 'false').lower() == 'true'
        if test_loading:
            self.test_model_loading()
        
        logger.info("모든 모델 다운로드 및 설정 완료")

if __name__ == "__main__":
    downloader = MistralLoRADownloader()
    downloader.download_all_models()