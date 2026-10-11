export type GuideSection = { h: string; ps: string[] }

export type Guide = {
  slug: string
  title: string
  summary: string
  intro: string
  sections: GuideSection[]
  ctaTitle: string
  ctaText: string
  ctaLabel: string
  ctaHref: string
}

const TESTE_CTA = { ctaTitle: 'Veja sua velocidade agora', ctaText: 'Leva cerca de 20 segundos.', ctaLabel: 'Fazer o teste', ctaHref: '/teste-de-velocidade' }
const APP_CTA = { ctaTitle: 'Descubra a causa no seu caso', ctaText: 'O SignallQ analisa sua rede e diz o que fazer.', ctaLabel: 'Baixar o app', ctaHref: '/#baixar' }

export const GUIDES: Guide[] = [
  {
    slug: 'por-que-minha-internet-esta-lenta',
    title: 'Por que minha internet está lenta?',
    summary: 'Wi-Fi, roteador, operadora ou DNS: aprenda a descobrir a causa da internet lenta e como resolver.',
    intro: 'Quase sempre o problema está em um destes lugares: o Wi-Fi, o roteador, a operadora ou o DNS. Descobrir qual é o primeiro passo para resolver.',
    ...APP_CTA,
    sections: [
      { h: 'Comece separando Wi-Fi de internet', ps: ['Faça um teste de velocidade perto do roteador e outro no cômodo onde a internet fica ruim. Se perto do roteador está boa e longe está fraca, o problema é o sinal do Wi-Fi, não a operadora.', 'Se está lenta nos dois lugares, teste também com cabo, se possível. Lenta no cabo aponta para o roteador ou para o plano contratado.'] },
      { h: 'Causas mais comuns', ps: ['Sinal fraco: distância, paredes, espelhos e eletrodomésticos como o micro-ondas enfraquecem o Wi-Fi.', 'Rede congestionada: muitos aparelhos usando ao mesmo tempo, ou vizinhos no mesmo canal de Wi-Fi.', 'Roteador com problema: aquecido, antigo ou ligado há muitas semanas sem reiniciar.', 'DNS lento: as páginas demoram a abrir, mesmo com a velocidade boa.', 'Operadora: instabilidade na região ou plano abaixo do que você precisa.'] },
      { h: 'O que fazer agora', ps: ['Reinicie o roteador: desligue da tomada por 30 segundos. Aproxime-se dele e teste de novo. Desconecte aparelhos que você não está usando.', 'Se nada mudar, anote os resultados dos testes e fale com a operadora. Os números ajudam o atendimento a localizar o problema.'] },
    ],
  },
  {
    slug: 'como-melhorar-sinal-wifi',
    title: 'Como melhorar o sinal do Wi-Fi em casa',
    summary: 'Posição do roteador, banda 2,4 e 5 GHz, canal e repetidor: ajustes que melhoram o Wi-Fi.',
    intro: 'Um Wi-Fi forte depende mais de onde o roteador está do que de quanto você paga pela internet. Estes ajustes resolvem a maioria dos casos.',
    ...APP_CTA,
    sections: [
      { h: 'Posicione bem o roteador', ps: ['Coloque-o no centro da casa, em lugar alto e aberto. Evite chão, armários, atrás da TV e perto de micro-ondas, telefones sem fio e aquários.', 'Paredes grossas e espelhos são os maiores bloqueios. Cada obstáculo no caminho reduz o sinal.'] },
      { h: 'Escolha a banda certa', ps: ['A banda de 2,4 GHz alcança mais longe, mas é mais lenta e sofre mais interferência. A de 5 GHz é mais rápida, mas alcança menos.', 'Perto do roteador, use 5 GHz. Em cômodos distantes, 2,4 GHz costuma funcionar melhor.'] },
      { h: 'Troque de canal se houver interferência', ps: ['Em prédios, muitas redes disputam o mesmo canal. Mudar o canal nas configurações do roteador pode melhorar a estabilidade.'] },
      { h: 'Quando comprar um repetidor ou rede mesh', ps: ['Se a casa é grande ou tem vários andares e o sinal não chega, um repetidor ou um sistema mesh resolve. Antes disso, teste o sinal em cada cômodo para saber onde ele cai.'] },
    ],
  },
  {
    slug: 'latencia-jitter-dns',
    title: 'O que são latência, jitter e DNS',
    summary: 'Entenda o que significam ping, jitter, perda de pacotes e DNS e por que importam além da velocidade.',
    intro: 'Velocidade não é tudo. Estas três medidas explicam por que a internet pode estar rápida e, mesmo assim, parecer ruim.',
    ...TESTE_CTA,
    sections: [
      { h: 'Latência (ping)', ps: ['É o tempo que um sinal leva para ir e voltar, medido em milissegundos. Quanto menor, mais rápida a resposta.', 'Importa em jogos online e videochamadas. Abaixo de 50 ms costuma ser ótimo; acima de 100 ms já dá para perceber atraso.'] },
      { h: 'Jitter', ps: ['É a variação da latência. Se o tempo de resposta oscila muito, a chamada engasga e o jogo dá saltos, mesmo com latência média baixa.'] },
      { h: 'Perda de pacotes', ps: ['Parte dos dados se perde no caminho e precisa ser reenviada. Causa travadas, áudio cortado e imagem congelada.'] },
      { h: 'DNS', ps: ['É a “lista telefônica” da internet: converte o nome de um site no endereço que o computador entende. Quando o DNS é lento, as páginas demoram a começar a carregar, mesmo com a velocidade boa.'] },
    ],
  },
  {
    slug: 'qual-velocidade-de-internet-preciso',
    title: 'Qual velocidade de internet eu preciso?',
    summary: 'Referência de Mbps para vídeo, 4K, videochamada e jogos, e como somar os aparelhos da casa.',
    intro: 'Depende de quantas pessoas usam ao mesmo tempo e do que fazem. Estes valores são uma referência para o dia a dia.',
    ...TESTE_CTA,
    sections: [
      { h: 'Referência por uso', ps: ['Redes sociais, mensagens e e-mail: 3 a 5 Mbps.', 'Vídeo em HD: cerca de 5 a 8 Mbps por tela.', 'Vídeo em 4K: cerca de 25 Mbps por tela.', 'Videochamada: 3 a 4 Mbps de download e também de upload.', 'Jogos online: pouca velocidade, mas latência baixa e estável.'] },
      { h: 'Some os aparelhos', ps: ['Some o que cada pessoa usa ao mesmo tempo. Uma casa com duas pessoas assistindo vídeo em HD e outra em videochamada precisa de algo perto de 25 Mbps.', 'Para quatro ou mais pessoas e vários aparelhos, 100 Mbps costuma dar folga.'] },
      { h: 'Teste o que você recebe de verdade', ps: ['A velocidade contratada é o máximo. No Wi-Fi, o que chega ao seu aparelho costuma ser menor. Teste no cômodo onde você usa a internet e compare com o plano.'] },
    ],
  },
]

export function getGuide(slug: string): Guide | undefined {
  return GUIDES.find((g) => g.slug === slug)
}

export function getRelatedGuides(slug: string, limit = 3): Guide[] {
  return GUIDES.filter((g) => g.slug !== slug).slice(0, limit)
}
